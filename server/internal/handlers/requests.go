package handlers

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/listings"
	"goodwill-server/internal/requests"
)

type RequestsHandler struct {
	requests *requests.Service
}

func NewRequestsHandler(r *requests.Service) *RequestsHandler {
	return &RequestsHandler{
		requests: r,
	}
}

type CreateDonationRequestInput struct {
	Message string `json:"message"`
}

func (h *RequestsHandler) CreateRequest(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	listingID := chi.URLParam(r, "id")
	if strings.TrimSpace(listingID) == "" {
		writeError(w, http.StatusBadRequest, "listing id is required")
		return
	}

	var input CreateDonationRequestInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	req, err := h.requests.CreateRequest(listingID, claims.UID, input.Message)
	if err != nil {
		switch {
		case errors.Is(err, requests.ErrEmptyMessage):
			writeError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, listings.ErrListingNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, requests.ErrListingNotDonation):
			writeError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, requests.ErrListingNotActive):
			writeError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, requests.ErrSelfRequest):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusCreated, req)
}

func (h *RequestsHandler) GetListingRequests(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	listingID := chi.URLParam(r, "id")
	if strings.TrimSpace(listingID) == "" {
		writeError(w, http.StatusBadRequest, "listing id is required")
		return
	}

	reqs, err := h.requests.GetListingRequests(listingID, claims.UID)
	if err != nil {
		switch {
		case errors.Is(err, listings.ErrListingNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, requests.ErrUnauthorized):
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	if reqs == nil {
		reqs = make([]*requests.DonationRequest, 0)
	}

	writeJSON(w, http.StatusOK, reqs)
}

func (h *RequestsHandler) GetIncomingRequests(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	reqs := h.requests.GetIncomingRequests(claims.UID)
	if reqs == nil {
		reqs = make([]*requests.DonationRequest, 0)
	}

	writeJSON(w, http.StatusOK, reqs)
}

func (h *RequestsHandler) AcceptRequest(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	requestID := chi.URLParam(r, "id")
	if strings.TrimSpace(requestID) == "" {
		writeError(w, http.StatusBadRequest, "request id is required")
		return
	}

	req, err := h.requests.AcceptRequest(requestID, claims.UID)
	if err != nil {
		switch {
		case errors.Is(err, requests.ErrRequestNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, requests.ErrUnauthorized):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, requests.ErrAlreadyResolved):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusOK, req)
}

func (h *RequestsHandler) DeclineRequest(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	requestID := chi.URLParam(r, "id")
	if strings.TrimSpace(requestID) == "" {
		writeError(w, http.StatusBadRequest, "request id is required")
		return
	}

	req, err := h.requests.DeclineRequest(requestID, claims.UID)
	if err != nil {
		switch {
		case errors.Is(err, requests.ErrRequestNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, requests.ErrUnauthorized):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, requests.ErrAlreadyResolved):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusOK, req)
}
