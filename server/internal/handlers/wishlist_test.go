package handlers_test

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/handlers"
	"goodwill-server/internal/listings"
	"goodwill-server/internal/users"
	"goodwill-server/internal/wishlist"
)

func setupWishlistEnv() (http.Handler, *wishlist.Service, *users.Service, *auth.MockVerifier) {
	userService := users.NewService()
	wishlistService := wishlist.NewService(userService)
	verifier := auth.NewMockVerifier()

	u1 := &users.User{
		UID:         "student-priya",
		Name:        "Priya Sharma",
		Email:       "priya@campus.edu",
		HostelBlock: "Hostel B (Girls)",
		RoomNo:      "204",
		Verified:    true,
	}
	userService.SaveUser(u1)

	u2 := &users.User{
		UID:         "student-intruder",
		Name:        "Intruder Student",
		Email:       "intruder@campus.edu",
		HostelBlock: "Hostel D",
		RoomNo:      "401",
		Verified:    true,
	}
	userService.SaveUser(u2)

	wishlistHandler := handlers.NewWishlistHandler(wishlistService, userService)

	r := chi.NewRouter()
	r.Route("/v1/wishlist", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Post("/", wishlistHandler.CreateWishlistItem)
		r.Get("/", wishlistHandler.ListWishlistItems)
		r.Get("/my", wishlistHandler.GetMyWishlistItems)
		r.Get("/{id}", wishlistHandler.GetWishlistItem)
		r.Delete("/{id}", wishlistHandler.DeleteWishlistItem)
	})

	return r, wishlistService, userService, verifier
}

func TestWishlist_CreateAndGet(t *testing.T) {
	router, _, _, _ := setupWishlistEnv()

	body, _ := json.Marshal(handlers.CreateWishlistRequest{
		Title:               "Scientific Calculator fx-991EX",
		Description:         "Needed for Semester 2 Applied Mathematics exam",
		Category:            listings.CategoryElectronics,
		IsDonationPreferred: true,
		MaxBudget:           0,
	})

	req := httptest.NewRequest(http.MethodPost, "/v1/wishlist", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-student-priya")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created, got %d: %s", rec.Code, rec.Body.String())
	}

	var created wishlist.WishlistItem
	if err := json.NewDecoder(rec.Body).Decode(&created); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}

	if created.Title != "Scientific Calculator fx-991EX" || created.RequesterID != "student-priya" {
		t.Fatalf("unexpected wishlist item details: %+v", created)
	}

	// Fetch by ID
	getReq := httptest.NewRequest(http.MethodGet, "/v1/wishlist/"+created.ID, nil)
	getReq.Header.Set("Authorization", "Bearer mock-token-student-priya")
	getRec := httptest.NewRecorder()
	router.ServeHTTP(getRec, getReq)

	if getRec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK on get by ID, got %d", getRec.Code)
	}
}

func TestWishlist_ListAndFilter(t *testing.T) {
	router, _, _, _ := setupWishlistEnv()

	// List all active wishlist items
	req := httptest.NewRequest(http.MethodGet, "/v1/wishlist", nil)
	req.Header.Set("Authorization", "Bearer mock-token-student-priya")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK, got %d", rec.Code)
	}

	var list []*wishlist.WishlistItem
	_ = json.NewDecoder(rec.Body).Decode(&list)
	if len(list) < 3 {
		t.Fatalf("expected at least 3 seed wishlist items, got %d", len(list))
	}

	// Filter by Category=STATIONERY
	filterReq := httptest.NewRequest(http.MethodGet, "/v1/wishlist?category=STATIONERY", nil)
	filterReq.Header.Set("Authorization", "Bearer mock-token-student-priya")
	filterRec := httptest.NewRecorder()
	router.ServeHTTP(filterRec, filterReq)

	var filtered []*wishlist.WishlistItem
	_ = json.NewDecoder(filterRec.Body).Decode(&filtered)
	for _, item := range filtered {
		if item.Category != listings.CategoryStationery {
			t.Errorf("expected CategoryStationery, got %s", item.Category)
		}
	}

	// Filter by Search=kettle
	searchReq := httptest.NewRequest(http.MethodGet, "/v1/wishlist?search=kettle", nil)
	searchReq.Header.Set("Authorization", "Bearer mock-token-student-priya")
	searchRec := httptest.NewRecorder()
	router.ServeHTTP(searchRec, searchReq)

	var searchResults []*wishlist.WishlistItem
	_ = json.NewDecoder(searchRec.Body).Decode(&searchResults)
	if len(searchResults) != 1 {
		t.Fatalf("expected 1 search result for kettle, got %d", len(searchResults))
	}
}

func TestWishlist_GetMyAndAuthorDelete(t *testing.T) {
	router, _, _, _ := setupWishlistEnv()

	// Fetch My Wishlist items for Priya (has seed item wish-seed-1)
	req := httptest.NewRequest(http.MethodGet, "/v1/wishlist/my", nil)
	req.Header.Set("Authorization", "Bearer mock-token-student-priya")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for my items, got %d", rec.Code)
	}

	var myItems []*wishlist.WishlistItem
	_ = json.NewDecoder(rec.Body).Decode(&myItems)
	if len(myItems) < 1 {
		t.Fatalf("expected at least 1 item for Priya, got %d", len(myItems))
	}

	targetID := myItems[0].ID

	// Delete item as author
	delReq := httptest.NewRequest(http.MethodDelete, "/v1/wishlist/"+targetID, nil)
	delReq.Header.Set("Authorization", "Bearer mock-token-student-priya")
	delRec := httptest.NewRecorder()
	router.ServeHTTP(delRec, delReq)

	if delRec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK on delete, got %d", delRec.Code)
	}

	// Should no longer appear in active list
	getReq := httptest.NewRequest(http.MethodGet, "/v1/wishlist/"+targetID, nil)
	getReq.Header.Set("Authorization", "Bearer mock-token-student-priya")
	getRec := httptest.NewRecorder()
	router.ServeHTTP(getRec, getReq)

	if getRec.Code != http.StatusNotFound {
		t.Fatalf("expected 404 Not Found after deletion, got %d", getRec.Code)
	}
}

func TestWishlist_UnauthorizedDelete(t *testing.T) {
	router, _, _, _ := setupWishlistEnv()

	// Intruder tries to delete Priya's seed item (wish-seed-1)
	delReq := httptest.NewRequest(http.MethodDelete, "/v1/wishlist/wish-seed-1", nil)
	delReq.Header.Set("Authorization", "Bearer mock-token-student-intruder")
	delRec := httptest.NewRecorder()
	router.ServeHTTP(delRec, delReq)

	if delRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 Forbidden for non-author delete, got %d", delRec.Code)
	}

	// Unauthenticated request
	unauthReq := httptest.NewRequest(http.MethodDelete, "/v1/wishlist/wish-seed-1", nil)
	unauthRec := httptest.NewRecorder()
	router.ServeHTTP(unauthRec, unauthReq)

	if unauthRec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 Unauthorized without token, got %d", unauthRec.Code)
	}
}
