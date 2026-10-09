package handlers

import (
	"encoding/json"
	"net/http"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/moderation"
)

type ModerationHandler struct {
	moderation *moderation.Service
}

func NewModerationHandler(m *moderation.Service) *ModerationHandler {
	return &ModerationHandler{
		moderation: m,
	}
}

type CreateReportRequest struct {
	Reason  moderation.ReportReason `json:"reason"`
	Comment string                  `json:"comment"`
}

func (h *ModerationHandler) SubmitReport(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	listingID := chi.URLParam(r, "id")
	if listingID == "" {
		writeError(w, http.StatusBadRequest, "listing ID is required")
		return
	}

	var req CreateReportRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	rep, err := h.moderation.SubmitReport(listingID, claims.UID, req.Reason, req.Comment)
	if err != nil {
		switch err {
		case moderation.ErrListingNotFound:
			writeError(w, http.StatusNotFound, err.Error())
		case moderation.ErrEmptyReason, moderation.ErrInvalidReason:
			writeError(w, http.StatusBadRequest, err.Error())
		case moderation.ErrSelfReporting:
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusCreated, rep)
}

func (h *ModerationHandler) GetListingReports(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	listingID := chi.URLParam(r, "id")
	if listingID == "" {
		writeError(w, http.StatusBadRequest, "listing ID is required")
		return
	}

	reports, err := h.moderation.GetReports(listingID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	if reports == nil {
		reports = make([]*moderation.Report, 0)
	}

	writeJSON(w, http.StatusOK, reports)
}
