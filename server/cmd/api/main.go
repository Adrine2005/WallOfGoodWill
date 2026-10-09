package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/chat"
	"goodwill-server/internal/config"
	"goodwill-server/internal/handlers"
	"goodwill-server/internal/invites"
	"goodwill-server/internal/listings"
	"goodwill-server/internal/moderation"
	"goodwill-server/internal/notifications"
	"goodwill-server/internal/points"
	"goodwill-server/internal/requests"
	"goodwill-server/internal/users"
	"goodwill-server/internal/wishlist"
)

func SetupRouter(userService *users.Service, inviteService *invites.Service, listingsService *listings.Service, requestsService *requests.Service, notifService *notifications.Service, chatService *chat.Service, wishlistService *wishlist.Service, pointsService *points.Service, moderationService *moderation.Service, verifier auth.TokenVerifier) *chi.Mux {
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
	chatHandler := handlers.NewChatHandler(chatService)
	wishlistHandler := handlers.NewWishlistHandler(wishlistService, userService)
	pointsHandler := handlers.NewPointsHandler(pointsService)
	moderationHandler := handlers.NewModerationHandler(moderationService)

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
			r.Post("/{id}/reports", moderationHandler.SubmitReport)
			r.Get("/{id}/reports", moderationHandler.GetListingReports)
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

	r.Route("/v1/chat", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Post("/conversations", chatHandler.CreateOrGetConversation)
		r.Get("/conversations", chatHandler.GetConversations)
		r.Get("/conversations/{id}", chatHandler.GetConversation)
		r.Get("/conversations/{id}/messages", chatHandler.GetMessages)
		r.Post("/conversations/{id}/messages", chatHandler.SendMessage)
	})

	r.Route("/v1/wishlist", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Post("/", wishlistHandler.CreateWishlistItem)
		r.Get("/", wishlistHandler.ListWishlistItems)
		r.Get("/my", wishlistHandler.GetMyWishlistItems)
		r.Get("/{id}", wishlistHandler.GetWishlistItem)
		r.Delete("/{id}", wishlistHandler.DeleteWishlistItem)
	})

	r.Route("/v1/points", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Get("/me", pointsHandler.GetMyPoints)
		r.Get("/leaderboard", pointsHandler.GetLeaderboard)
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
	chatService := chat.NewService(listingsService, userService, notifService)
	wishlistService := wishlist.NewService(userService)
	pointsService := points.NewService(userService)
	moderationService := moderation.NewService(listingsService)
	requestsService.SetPointsService(pointsService)
	verifier := auth.NewMockVerifier()

	r := SetupRouter(userService, inviteService, listingsService, requestsService, notifService, chatService, wishlistService, pointsService, moderationService, verifier)

	addr := fmt.Sprintf(":%s", cfg.Port)
	log.Printf("Wall of Goodwill server starting on port %s in %s mode\n", cfg.Port, cfg.Env)
	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
