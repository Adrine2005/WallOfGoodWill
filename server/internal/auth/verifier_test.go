package auth_test

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	"goodwill-server/internal/auth"
)

func TestMockVerifier(t *testing.T) {
	mv := auth.NewMockVerifier()

	// Empty token fails
	_, err := mv.VerifyToken(context.Background(), "")
	if err == nil {
		t.Errorf("expected error for empty token")
	}

	// Valid mock token
	claims, err := mv.VerifyToken(context.Background(), "mock-token-adrine")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if claims.UID != "adrine" {
		t.Errorf("expected UID 'adrine', got %q", claims.UID)
	}

	// Set claims
	err = mv.SetUserClaims(context.Background(), "adrine", map[string]interface{}{
		"verified": true,
		"role":     "admin",
	})
	if err != nil {
		t.Fatalf("failed to set user claims: %v", err)
	}

	updated, err := mv.VerifyToken(context.Background(), "adrine")
	if err != nil {
		t.Fatalf("failed to verify after setting claims: %v", err)
	}

	if !updated.Verified {
		t.Errorf("expected verified true")
	}
	if updated.Role != "admin" {
		t.Errorf("expected role admin, got %q", updated.Role)
	}
}

func TestAuthMiddleware(t *testing.T) {
	mv := auth.NewMockVerifier()
	middleware := auth.Middleware(mv)

	nextHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		claims, ok := auth.GetUserClaims(r.Context())
		if !ok || claims == nil {
			t.Errorf("claims not found in context")
			http.Error(w, "no claims", http.StatusInternalServerError)
			return
		}
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(claims.UID))
	})

	// Missing header -> 401
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	rr := httptest.NewRecorder()
	middleware(nextHandler).ServeHTTP(rr, req)
	if rr.Code != http.StatusUnauthorized {
		t.Errorf("expected status 401, got %d", rr.Code)
	}

	// Valid Bearer token -> 200
	req = httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("Authorization", "Bearer mock-token-student1")
	rr = httptest.NewRecorder()
	middleware(nextHandler).ServeHTTP(rr, req)
	if rr.Code != http.StatusOK {
		t.Errorf("expected status 200, got %d", rr.Code)
	}
	if rr.Body.String() != "student1" {
		t.Errorf("expected body student1, got %q", rr.Body.String())
	}
}
