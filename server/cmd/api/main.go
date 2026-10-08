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
	"goodwill-server/internal/users"
)

func SetupRouter(userService *users.Service, inviteService *invites.Service, verifier auth.TokenVerifier) *chi.Mux {
	r := chi.NewRouter()

	r.Use(middleware.RequestID)
	r.Use(middleware.RealIP)
	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)

	r.Get("/health", handlers.Health)

	authHandler := handlers.NewAuthHandler(userService, inviteService, verifier)
	adminHandler := handlers.NewAdminHandler(inviteService)

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

	return r
}

func main() {
	cfg := config.Load()

	userService := users.NewService()
	inviteService := invites.NewService()
	verifier := auth.NewMockVerifier()

	r := SetupRouter(userService, inviteService, verifier)

	addr := fmt.Sprintf(":%s", cfg.Port)
	log.Printf("Wall of Goodwill server starting on port %s in %s mode\n", cfg.Port, cfg.Env)
	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
