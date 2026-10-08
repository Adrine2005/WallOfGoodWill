package auth

import (
	"context"
	"errors"
	"net/http"
	"strings"
	"sync"
)

type contextKey string

const userClaimsContextKey contextKey = "userClaims"

type UserClaims struct {
	UID      string                 `json:"uid"`
	Email    string                 `json:"email"`
	Role     string                 `json:"role"`
	Verified bool                   `json:"verified"`
	Claims   map[string]interface{} `json:"claims"`
}

type TokenVerifier interface {
	VerifyToken(ctx context.Context, tokenString string) (*UserClaims, error)
	SetUserClaims(ctx context.Context, uid string, claims map[string]interface{}) error
}

// MockVerifier provides an in-memory token verification mechanism for dev and testing
type MockVerifier struct {
	mu     sync.RWMutex
	claims map[string]*UserClaims
}

func NewMockVerifier() *MockVerifier {
	return &MockVerifier{
		claims: make(map[string]*UserClaims),
	}
}

func (m *MockVerifier) VerifyToken(ctx context.Context, token string) (*UserClaims, error) {
	if strings.TrimSpace(token) == "" {
		return nil, errors.New("missing authorization token")
	}

	m.mu.RLock()
	defer m.mu.RUnlock()

	// If token has been registered in memory, return it
	if user, exists := m.claims[token]; exists {
		return user, nil
	}

	// Dynamic mock token convention: "mock-token-<uid>" or "dev-<uid>"
	cleanToken := strings.TrimSpace(token)
	uid := "dev-student-123"
	email := "student@campus.edu"

	if strings.HasPrefix(cleanToken, "mock-token-") {
		uid = strings.TrimPrefix(cleanToken, "mock-token-")
		email = uid + "@campus.edu"
	}

	// Also check if claims exist for this uid
	if user, exists := m.claims[uid]; exists {
		return user, nil
	}

	defaultClaims := &UserClaims{
		UID:      uid,
		Email:    email,
		Role:     "student",
		Verified: false,
		Claims:   map[string]interface{}{"verified": false, "role": "student"},
	}

	return defaultClaims, nil
}

func (m *MockVerifier) SetUserClaims(ctx context.Context, uid string, claims map[string]interface{}) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	user, exists := m.claims[uid]
	if !exists {
		user = &UserClaims{
			UID:    uid,
			Claims: make(map[string]interface{}),
		}
		m.claims[uid] = user
	}

	for k, v := range claims {
		user.Claims[k] = v
		if k == "verified" {
			if verifiedBool, ok := v.(bool); ok {
				user.Verified = verifiedBool
			}
		}
		if k == "role" {
			if roleStr, ok := v.(string); ok {
				user.Role = roleStr
			}
		}
	}

	return nil
}

// Middleware extracts Bearer token and verifies using the TokenVerifier
func Middleware(verifier TokenVerifier) func(next http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				http.Error(w, `{"error":"missing authorization header"}`, http.StatusUnauthorized)
				return
			}

			parts := strings.SplitN(authHeader, " ", 2)
			if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
				http.Error(w, `{"error":"invalid authorization format"}`, http.StatusUnauthorized)
				return
			}

			token := parts[1]
			claims, err := verifier.VerifyToken(r.Context(), token)
			if err != nil {
				http.Error(w, `{"error":"invalid or expired token"}`, http.StatusUnauthorized)
				return
			}

			ctx := context.WithValue(r.Context(), userClaimsContextKey, claims)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func GetUserClaims(ctx context.Context) (*UserClaims, bool) {
	claims, ok := ctx.Value(userClaimsContextKey).(*UserClaims)
	return claims, ok
}
