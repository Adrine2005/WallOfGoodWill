package handlers

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/listings"
	"goodwill-server/internal/users"
)

type ListingsHandler struct {
	listings *listings.Service
	users    *users.Service
}

func NewListingsHandler(l *listings.Service, u *users.Service) *ListingsHandler {
	return &ListingsHandler{
		listings: l,
		users:    u,
	}
}

type CreateListingRequest struct {
	Title       string                 `json:"title"`
	Description string                 `json:"description"`
	Category    listings.ItemCategory  `json:"category"`
	Type        listings.ItemType      `json:"type"`
	Price       float64                `json:"price"`
	Condition   listings.ItemCondition `json:"condition"`
	Photos      []string               `json:"photos"`
}

func (h *ListingsHandler) CreateListing(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req CreateListingRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	sellerName := "Campus Student"
	hostelBlock := "General Campus"
	roomNo := "N/A"

	user, err := h.users.GetUser(claims.UID)
	if err == nil && user != nil {
		if user.Name != "" {
			sellerName = user.Name
		}
		if user.HostelBlock != "" {
			hostelBlock = user.HostelBlock
		}
		if user.RoomNo != "" {
			roomNo = user.RoomNo
		}
	}

	item, err := h.listings.CreateListing(
		claims.UID,
		sellerName,
		hostelBlock,
		roomNo,
		req.Title,
		req.Description,
		req.Category,
		req.Type,
		req.Price,
		req.Condition,
		req.Photos,
	)

	if err != nil {
		switch {
		case errors.Is(err, listings.ErrInvalidPrice):
			writeError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, listings.ErrInvalidInput):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusCreated, item)
}

func (h *ListingsHandler) ListListings(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	filter := listings.Filter{
		Category:    listings.ItemCategory(strings.ToUpper(strings.TrimSpace(q.Get("category")))),
		Type:        listings.ItemType(strings.ToUpper(strings.TrimSpace(q.Get("type")))),
		HostelBlock: strings.TrimSpace(q.Get("hostelBlock")),
		Search:      strings.TrimSpace(q.Get("search")),
	}

	items := h.listings.ListListings(filter)
	if items == nil {
		items = make([]*listings.Listing, 0)
	}
	writeJSON(w, http.StatusOK, items)
}

func (h *ListingsHandler) GetListing(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	item, err := h.listings.GetListing(id)
	if err != nil {
		if errors.Is(err, listings.ErrListingNotFound) {
			writeError(w, http.StatusNotFound, "listing not found")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusOK, item)
}

func (h *ListingsHandler) GetMyListings(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	items := h.listings.GetMyListings(claims.UID)
	if items == nil {
		items = make([]*listings.Listing, 0)
	}
	writeJSON(w, http.StatusOK, items)
}

func (h *ListingsHandler) DeleteListing(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	id := chi.URLParam(r, "id")
	err := h.listings.DeleteListing(id, claims.UID)
	if err != nil {
		switch {
		case errors.Is(err, listings.ErrListingNotFound):
			writeError(w, http.StatusNotFound, "listing not found")
		case errors.Is(err, listings.ErrUnauthorized):
			writeError(w, http.StatusForbidden, "unauthorized: only the seller can delete this listing")
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]string{"message": "listing deleted successfully"})
}

type UpdateStatusRequest struct {
	Status listings.ListingStatus `json:"status"`
}

func (h *ListingsHandler) UpdateListingStatus(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	id := chi.URLParam(r, "id")
	var req UpdateStatusRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	status := listings.ListingStatus(strings.ToUpper(strings.TrimSpace(string(req.Status))))
	if status != listings.StatusActive && status != listings.StatusSold && status != listings.StatusDonated && status != listings.StatusDeleted {
		writeError(w, http.StatusBadRequest, "invalid status value")
		return
	}

	err := h.listings.UpdateStatus(id, claims.UID, status)
	if err != nil {
		switch {
		case errors.Is(err, listings.ErrListingNotFound):
			writeError(w, http.StatusNotFound, "listing not found")
		case errors.Is(err, listings.ErrUnauthorized):
			writeError(w, http.StatusForbidden, "unauthorized: only the seller can update status")
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]string{
		"message": "status updated successfully",
		"status":  string(status),
	})
}
