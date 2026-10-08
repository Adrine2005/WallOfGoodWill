package handlers

import (
	"encoding/json"
	"net/http"
	"strings"
	"time"

	"goodwill-server/internal/invites"
)

type AdminHandler struct {
	invites *invites.Service
}

func NewAdminHandler(inv *invites.Service) *AdminHandler {
	return &AdminHandler{
		invites: inv,
	}
}

type GenerateQRRequest struct {
	Label           string `json:"label"`
	DurationSeconds int64  `json:"durationSeconds"`
	MaxUses         int    `json:"maxUses"`
}

func (h *AdminHandler) GenerateQR(w http.ResponseWriter, r *http.Request) {
	var req GenerateQRRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request payload")
		return
	}

	req.Label = strings.TrimSpace(req.Label)
	if req.Label == "" {
		req.Label = "Hostel Notice Board"
	}

	duration := time.Duration(req.DurationSeconds) * time.Second
	if req.DurationSeconds <= 0 {
		duration = 24 * time.Hour
	}

	invite, err := h.invites.CreateInvite(req.Label, duration, req.MaxUses)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to generate invite code")
		return
	}

	writeJSON(w, http.StatusCreated, invite)
}

func (h *AdminHandler) ListQR(w http.ResponseWriter, r *http.Request) {
	list := h.invites.ListInvites()
	writeJSON(w, http.StatusOK, list)
}
