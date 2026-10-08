package handlers

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/invites"
	"goodwill-server/internal/users"
)

type AuthHandler struct {
	users    *users.Service
	invites  *invites.Service
	verifier auth.TokenVerifier
}

func NewAuthHandler(u *users.Service, inv *invites.Service, v auth.TokenVerifier) *AuthHandler {
	return &AuthHandler{
		users:    u,
		invites:  inv,
		verifier: v,
	}
}

type RegisterRequest struct {
	UID         string `json:"uid,omitempty"`
	Name        string `json:"name"`
	Email       string `json:"email"`
	HostelBlock string `json:"hostelBlock"`
	RoomNo      string `json:"roomNo"`
	Phone       string `json:"phone,omitempty"`
	InviteCode  string `json:"inviteCode,omitempty"`
}

type VerifyQRRequest struct {
	Code string `json:"code"`
}

type AuthResponse struct {
	Verified bool        `json:"verified"`
	Message  string      `json:"message"`
	User     *users.User `json:"user,omitempty"`
}

type ErrorResponse struct {
	Error string `json:"error"`
}

func writeJSON(w http.ResponseWriter, status int, data interface{}) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(data)
}

func writeError(w http.ResponseWriter, status int, message string) {
	writeJSON(w, status, ErrorResponse{Error: message})
}

// Register creates or updates student profile, with optional instant QR validation
func (h *AuthHandler) Register(w http.ResponseWriter, r *http.Request) {
	var req RegisterRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request payload")
		return
	}

	req.Name = strings.TrimSpace(req.Name)
	req.Email = strings.TrimSpace(req.Email)
	req.HostelBlock = strings.TrimSpace(req.HostelBlock)
	req.RoomNo = strings.TrimSpace(req.RoomNo)
	req.InviteCode = strings.TrimSpace(req.InviteCode)

	if req.Name == "" {
		writeError(w, http.StatusBadRequest, "name is required")
		return
	}
	if req.HostelBlock == "" {
		writeError(w, http.StatusBadRequest, "hostel block is required")
		return
	}
	if req.RoomNo == "" {
		writeError(w, http.StatusBadRequest, "room number is required")
		return
	}

	uid := strings.TrimSpace(req.UID)
	claims, hasClaims := auth.GetUserClaims(r.Context())
	if hasClaims && claims.UID != "" {
		uid = claims.UID
		if req.Email == "" && claims.Email != "" {
			req.Email = claims.Email
		}
	}

	if uid == "" {
		writeError(w, http.StatusBadRequest, "user id is required")
		return
	}
	if req.Email == "" {
		writeError(w, http.StatusBadRequest, "email is required")
		return
	}

	user := &users.User{
		UID:         uid,
		Name:        req.Name,
		Email:       req.Email,
		HostelBlock: req.HostelBlock,
		RoomNo:      req.RoomNo,
		Phone:       strings.TrimSpace(req.Phone),
		Role:        "student",
		Verified:    false,
	}

	// If invite code provided, validate and instantly verify
	if req.InviteCode != "" {
		invite, err := h.invites.ValidateAndUseInvite(req.InviteCode)
		if err != nil {
			switch {
			case errors.Is(err, invites.ErrInviteNotFound):
				writeError(w, http.StatusNotFound, "invite code not found")
			case errors.Is(err, invites.ErrInviteExpired):
				writeError(w, http.StatusBadRequest, "invite code has expired")
			case errors.Is(err, invites.ErrInviteExhausted):
				writeError(w, http.StatusBadRequest, "invite code usage limit reached")
			case errors.Is(err, invites.ErrInviteInactive):
				writeError(w, http.StatusBadRequest, "invite code is inactive")
			default:
				writeError(w, http.StatusBadRequest, err.Error())
			}
			return
		}

		user.Verified = true
		user.VerifiedViaCode = invite.Code
		_ = h.verifier.SetUserClaims(r.Context(), user.UID, map[string]interface{}{
			"verified": true,
			"role":     user.Role,
		})
	}

	h.users.SaveUser(user)

	writeJSON(w, http.StatusCreated, AuthResponse{
		Verified: user.Verified,
		Message:  "user registered successfully",
		User:     user,
	})
}

// VerifyQR validates a submitted campus QR code for an authenticated user
func (h *AuthHandler) VerifyQR(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req VerifyQRRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request payload")
		return
	}

	req.Code = strings.TrimSpace(req.Code)
	if req.Code == "" {
		writeError(w, http.StatusBadRequest, "invite code is required")
		return
	}

	user, err := h.users.GetUser(claims.UID)
	if err != nil {
		writeError(w, http.StatusNotFound, "user profile not found")
		return
	}

	invite, err := h.invites.ValidateAndUseInvite(req.Code)
	if err != nil {
		switch {
		case errors.Is(err, invites.ErrInviteNotFound):
			writeError(w, http.StatusNotFound, "invite code not found")
		case errors.Is(err, invites.ErrInviteExpired):
			writeError(w, http.StatusBadRequest, "invite code has expired")
		case errors.Is(err, invites.ErrInviteExhausted):
			writeError(w, http.StatusBadRequest, "invite code usage limit reached")
		case errors.Is(err, invites.ErrInviteInactive):
			writeError(w, http.StatusBadRequest, "invite code is inactive")
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	user.Verified = true
	user.VerifiedViaCode = invite.Code
	h.users.SaveUser(user)

	_ = h.verifier.SetUserClaims(r.Context(), user.UID, map[string]interface{}{
		"verified": true,
		"role":     user.Role,
	})

	writeJSON(w, http.StatusOK, AuthResponse{
		Verified: true,
		Message:  "campus verification successful",
		User:     user,
	})
}

// Me returns the current authenticated user's profile and verification status
func (h *AuthHandler) Me(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	user, err := h.users.GetUser(claims.UID)
	if err != nil {
		writeError(w, http.StatusNotFound, "user profile not found")
		return
	}

	writeJSON(w, http.StatusOK, user)
}
