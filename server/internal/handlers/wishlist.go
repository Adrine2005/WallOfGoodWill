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
	"goodwill-server/internal/wishlist"
)

type WishlistHandler struct {
	wishlist *wishlist.Service
	users    *users.Service
}

func NewWishlistHandler(w *wishlist.Service, u *users.Service) *WishlistHandler {
	return &WishlistHandler{
		wishlist: w,
		users:    u,
	}
}

type CreateWishlistRequest struct {
	Title               string                `json:"title"`
	Description         string                `json:"description"`
	Category            listings.ItemCategory `json:"category"`
	IsDonationPreferred bool                  `json:"isDonationPreferred"`
	MaxBudget           float64               `json:"maxBudget"`
}

func (h *WishlistHandler) CreateWishlistItem(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req CreateWishlistRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	if strings.TrimSpace(req.Title) == "" {
		writeError(w, http.StatusBadRequest, "title is required")
		return
	}

	userName := claims.Email
	hostelBlock := ""
	roomNo := ""
	if h.users != nil {
		user, _ := h.users.GetUser(claims.UID)
		if user != nil {
			userName = user.Name
			hostelBlock = user.HostelBlock
			roomNo = user.RoomNo
		}
	}

	item, err := h.wishlist.CreateWishlistItem(
		claims.UID, userName, hostelBlock, roomNo,
		req.Title, req.Description, req.Category,
		req.IsDonationPreferred, req.MaxBudget,
	)
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	writeJSON(w, http.StatusCreated, item)
}

func (h *WishlistHandler) ListWishlistItems(w http.ResponseWriter, r *http.Request) {
	category := r.URL.Query().Get("category")
	search := r.URL.Query().Get("search")

	items := h.wishlist.ListWishlistItems(category, search)
	if items == nil {
		items = make([]*wishlist.WishlistItem, 0)
	}

	writeJSON(w, http.StatusOK, items)
}

func (h *WishlistHandler) GetMyWishlistItems(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	items := h.wishlist.GetMyWishlistItems(claims.UID)
	if items == nil {
		items = make([]*wishlist.WishlistItem, 0)
	}

	writeJSON(w, http.StatusOK, items)
}

func (h *WishlistHandler) GetWishlistItem(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "id")
	if strings.TrimSpace(id) == "" {
		writeError(w, http.StatusBadRequest, "id is required")
		return
	}

	item, err := h.wishlist.GetWishlistItem(id)
	if err != nil {
		if errors.Is(err, wishlist.ErrWishlistNotFound) {
			writeError(w, http.StatusNotFound, "wishlist item not found")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusOK, item)
}

func (h *WishlistHandler) DeleteWishlistItem(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	id := chi.URLParam(r, "id")
	if strings.TrimSpace(id) == "" {
		writeError(w, http.StatusBadRequest, "id is required")
		return
	}

	err := h.wishlist.DeleteWishlistItem(id, claims.UID)
	if err != nil {
		if errors.Is(err, wishlist.ErrWishlistNotFound) {
			writeError(w, http.StatusNotFound, "wishlist item not found")
			return
		}
		if errors.Is(err, wishlist.ErrUnauthorized) {
			writeError(w, http.StatusForbidden, "unauthorized: only the requester can delete this item")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusOK, map[string]string{"message": "wishlist item deleted"})
}
