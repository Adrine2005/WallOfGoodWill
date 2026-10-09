package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/config"
	"goodwill-server/internal/handlers"
	"goodwill-server/internal/invites"
	"goodwill-server/internal/listings"
	"goodwill-server/internal/notifications"
	"goodwill-server/internal/requests"
	"goodwill-server/internal/users"
)

func SetupRouter(userService *users.Service, inviteService *invites.Service, listingsService *listings.Service, requestsService *requests.Service, notifService *notifications.Service, verifier auth.TokenVerifier) *chi.Mux {
	r := chi.NewRouter()

	r.Use(middleware.RequestID)
	r.Use(middleware.RealIP)
	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)

	r.Get("/health", handlers.Health)

	authHandler := handlers.NewAuthHandler(userService, inviteService, verifier)
	adminHandler := handlers.NewAdminHandler(inviteService)
	listingsHandler := handlers.NewListingsHandler(listingsService, userService)
	requestsHandler := handlers.NewRequestsHandler(requestsService)
	notifHandler := handlers.NewNotificationsHandler(notifService)

	r.Route("/v1/auth", func(r chi.Router) {
		r.Post("/register", authHandler.Register)

		r.Group(func(r chi.Router) {
			r.Use(auth.Middleware(verifier))
			r.Post("/verify-qr", authHandler.VerifyQR)
			r.Get("/me", authHandler.Me)
		})
	})

	r.Route("/v1/admin", func(r chi.Router) {
		r.Post("/qr/generate", adminHandler.GenerateQR)
		r.Get("/qr/list", adminHandler.ListQR)
	})

	r.Route("/v1/listings", func(r chi.Router) {
		r.Get("/", listingsHandler.ListListings)
		r.Get("/{id}", listingsHandler.GetListing)

		r.Group(func(r chi.Router) {
			r.Use(auth.Middleware(verifier))
			r.Post("/", listingsHandler.CreateListing)
			r.Get("/my", listingsHandler.GetMyListings)
			r.Patch("/{id}", listingsHandler.UpdateListing)
			r.Put("/{id}/status", listingsHandler.UpdateListingStatus)
			r.Delete("/{id}", listingsHandler.DeleteListing)
			r.Post("/{id}/requests", requestsHandler.CreateRequest)
			r.Get("/{id}/requests", requestsHandler.GetListingRequests)
		})
	})

	r.Route("/v1/requests", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Get("/incoming", requestsHandler.GetIncomingRequests)
		r.Post("/{id}/accept", requestsHandler.AcceptRequest)
		r.Post("/{id}/decline", requestsHandler.DeclineRequest)
	})

	r.Route("/v1/notifications", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Get("/", notifHandler.GetNotifications)
		r.Get("/unread-count", notifHandler.GetUnreadCount)
		r.Post("/{id}/read", notifHandler.MarkAsRead)
		r.Post("/read-all", notifHandler.MarkAllAsRead)
	})

	return r
}

func main() {
	cfg := config.Load()

	userService := users.NewService()
	inviteService := invites.NewService()
	listingsService := listings.NewService()
	notifService := notifications.NewService()
	requestsService := requests.NewService(listingsService, userService, notifService)
	verifier := auth.NewMockVerifier()

	r := SetupRouter(userService, inviteService, listingsService, requestsService, notifService, verifier)

	addr := fmt.Sprintf(":%s", cfg.Port)
	log.Printf("Wall of Goodwill server starting on port %s in %s mode\n", cfg.Port, cfg.Env)
	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
