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
)

func setupListingsEnv() (http.Handler, *listings.Service, *users.Service, *auth.MockVerifier) {
	userService := users.NewService()
	listingsService := listings.NewService()
	verifier := auth.NewMockVerifier()

	// Register a test student profile
	user := &users.User{
		UID:         "student-seller-1",
		Name:        "Siddharth Rao",
		Email:       "sid@campus.edu",
		HostelBlock: "Hostel Block A (Boys)",
		RoomNo:      "204",
		Verified:    true,
	}
	userService.SaveUser(user)

	listingsHandler := handlers.NewListingsHandler(listingsService, userService)

	r := chi.NewRouter()
	r.Route("/v1/listings", func(r chi.Router) {
		r.Get("/", listingsHandler.ListListings)
		r.Get("/{id}", listingsHandler.GetListing)

		r.Group(func(r chi.Router) {
			r.Use(auth.Middleware(verifier))
			r.Post("/", listingsHandler.CreateListing)
			r.Get("/my", listingsHandler.GetMyListings)
			r.Put("/{id}/status", listingsHandler.UpdateListingStatus)
			r.Delete("/{id}", listingsHandler.DeleteListing)
		})
	})

	return r, listingsService, userService, verifier
}

func TestListListings_ReturnsSeedItems(t *testing.T) {
	router, _, _, _ := setupListingsEnv()

	req := httptest.NewRequest(http.MethodGet, "/v1/listings", nil)
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK, got %d", rec.Code)
	}

	var items []*listings.Listing
	if err := json.NewDecoder(rec.Body).Decode(&items); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}

	if len(items) < 4 {
		t.Errorf("expected at least 4 items, got %d", len(items))
	}
}

func TestCreateListing_DonateAndSell(t *testing.T) {
	router, _, _, _ := setupListingsEnv()

	// 1. Create Donation listing
	donateReq := map[string]interface{}{
		"title":       "Data Structures in C++",
		"description": "Standard college textbook",
		"category":    "BOOKS",
		"type":        "DONATE",
		"price":       100, // Should be normalized to 0
		"condition":   "GOOD",
	}
	donateBytes, _ := json.Marshal(donateReq)

	req1 := httptest.NewRequest(http.MethodPost, "/v1/listings", bytes.NewReader(donateBytes))
	req1.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	rec1 := httptest.NewRecorder()
	router.ServeHTTP(rec1, req1)

	if rec1.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created for donation, got %d. body: %s", rec1.Code, rec1.Body.String())
	}

	var createdDonation listings.Listing
	_ = json.NewDecoder(rec1.Body).Decode(&createdDonation)
	if createdDonation.Price != 0 {
		t.Errorf("expected price 0, got %f", createdDonation.Price)
	}
	if createdDonation.SellerName != "Siddharth Rao" {
		t.Errorf("expected seller name Siddharth Rao, got %s", createdDonation.SellerName)
	}

	// 2. Create Sale listing with positive price
	saleReq := map[string]interface{}{
		"title":       "Scientific Calculator Casio fx-991EX",
		"description": "Used for 1 semester, perfectly working",
		"category":    "ELECTRONICS",
		"type":        "SELL",
		"price":       750,
		"condition":   "LIKE_NEW",
	}
	saleBytes, _ := json.Marshal(saleReq)

	req2 := httptest.NewRequest(http.MethodPost, "/v1/listings", bytes.NewReader(saleBytes))
	req2.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	rec2 := httptest.NewRecorder()
	router.ServeHTTP(rec2, req2)

	if rec2.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created for sale, got %d. body: %s", rec2.Code, rec2.Body.String())
	}

	// 3. Create Sale listing with invalid price (<= 0)
	invalidSaleReq := map[string]interface{}{
		"title":       "Bad Sale Item",
		"description": "Should fail",
		"category":    "OTHER",
		"type":        "SELL",
		"price":       0,
	}
	invalidBytes, _ := json.Marshal(invalidSaleReq)

	req3 := httptest.NewRequest(http.MethodPost, "/v1/listings", bytes.NewReader(invalidBytes))
	req3.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	rec3 := httptest.NewRecorder()
	router.ServeHTTP(rec3, req3)

	if rec3.Code != http.StatusBadRequest {
		t.Errorf("expected 400 Bad Request for zero sale price, got %d", rec3.Code)
	}
}

func TestFilterAndSearchListings(t *testing.T) {
	router, _, _, _ := setupListingsEnv()

	// Filter by Category BOOKS
	reqCat := httptest.NewRequest(http.MethodGet, "/v1/listings?category=BOOKS", nil)
	recCat := httptest.NewRecorder()
	router.ServeHTTP(recCat, reqCat)

	var books []*listings.Listing
	_ = json.NewDecoder(recCat.Body).Decode(&books)
	for _, b := range books {
		if b.Category != listings.CategoryBooks {
			t.Errorf("expected only BOOKS, got %s", b.Category)
		}
	}

	// Filter by Type DONATE
	reqType := httptest.NewRequest(http.MethodGet, "/v1/listings?type=DONATE", nil)
	recType := httptest.NewRecorder()
	router.ServeHTTP(recType, reqType)

	var donations []*listings.Listing
	_ = json.NewDecoder(recType.Body).Decode(&donations)
	for _, d := range donations {
		if d.Type != listings.TypeDonate {
			t.Errorf("expected only DONATE, got %s", d.Type)
		}
	}
}

func TestDeleteListing_Permissions(t *testing.T) {
	router, listingsService, _, _ := setupListingsEnv()

	item, _ := listingsService.CreateListing(
		"student-seller-1",
		"Sid",
		"Block A",
		"204",
		"Notebook",
		"Blank notebook",
		listings.CategoryStationery,
		listings.TypeDonate,
		0,
		listings.ConditionNew,
		nil,
	)

	// Unauthorized delete attempt by student-other
	reqUnauth := httptest.NewRequest(http.MethodDelete, "/v1/listings/"+item.ID, nil)
	reqUnauth.Header.Set("Authorization", "Bearer mock-token-student-other")
	recUnauth := httptest.NewRecorder()
	router.ServeHTTP(recUnauth, reqUnauth)

	if recUnauth.Code != http.StatusForbidden {
		t.Errorf("expected 403 Forbidden for non-owner delete, got %d", recUnauth.Code)
	}

	// Authorized delete attempt by owner
	reqAuth := httptest.NewRequest(http.MethodDelete, "/v1/listings/"+item.ID, nil)
	reqAuth.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	recAuth := httptest.NewRecorder()
	router.ServeHTTP(recAuth, reqAuth)

	if recAuth.Code != http.StatusOK {
		t.Errorf("expected 200 OK for owner delete, got %d", recAuth.Code)
	}
}

func TestUpdateListingStatus(t *testing.T) {
	router, listingsService, _, _ := setupListingsEnv()

	item, err := listingsService.CreateListing(
		"student-seller-1",
		"Sid",
		"Block A",
		"204",
		"Electric Kettle",
		"1.5L kettle in good condition",
		listings.CategoryKitchenware,
		listings.TypeSell,
		350,
		listings.ConditionGood,
		nil,
	)
	if err != nil {
		t.Fatalf("failed to create listing: %v", err)
	}

	// 1. Unauthorized attempt (different student)
	bodyOther, _ := json.Marshal(map[string]string{"status": "SOLD"})
	reqOther := httptest.NewRequest(http.MethodPut, "/v1/listings/"+item.ID+"/status", bytes.NewReader(bodyOther))
	reqOther.Header.Set("Authorization", "Bearer mock-token-student-other")
	recOther := httptest.NewRecorder()
	router.ServeHTTP(recOther, reqOther)
	if recOther.Code != http.StatusForbidden {
		t.Errorf("expected 403 Forbidden for non-owner update, got %d", recOther.Code)
	}

	// 2. Invalid status value
	bodyInvalid, _ := json.Marshal(map[string]string{"status": "UNKNOWN_STATUS"})
	reqInvalid := httptest.NewRequest(http.MethodPut, "/v1/listings/"+item.ID+"/status", bytes.NewReader(bodyInvalid))
	reqInvalid.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	recInvalid := httptest.NewRecorder()
	router.ServeHTTP(recInvalid, reqInvalid)
	if recInvalid.Code != http.StatusBadRequest {
		t.Errorf("expected 400 Bad Request for invalid status, got %d", recInvalid.Code)
	}

	// 3. Not found listing
	bodyNotFound, _ := json.Marshal(map[string]string{"status": "SOLD"})
	reqNotFound := httptest.NewRequest(http.MethodPut, "/v1/listings/non-existent-id/status", bytes.NewReader(bodyNotFound))
	reqNotFound.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	recNotFound := httptest.NewRecorder()
	router.ServeHTTP(recNotFound, reqNotFound)
	if recNotFound.Code != http.StatusNotFound {
		t.Errorf("expected 404 Not Found, got %d", recNotFound.Code)
	}

	// 4. Authorized status update to SOLD
	bodySold, _ := json.Marshal(map[string]string{"status": "SOLD"})
	reqSold := httptest.NewRequest(http.MethodPut, "/v1/listings/"+item.ID+"/status", bytes.NewReader(bodySold))
	reqSold.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	recSold := httptest.NewRecorder()
	router.ServeHTTP(recSold, reqSold)
	if recSold.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for owner update to SOLD, got %d: %s", recSold.Code, recSold.Body.String())
	}

	// Verify listing is now SOLD in service
	updatedItem, err := listingsService.GetListing(item.ID)
	if err != nil {
		t.Fatalf("failed to get listing: %v", err)
	}
	if updatedItem.Status != listings.StatusSold {
		t.Errorf("expected status %s, got %s", listings.StatusSold, updatedItem.Status)
	}

	// 5. Authorized status update to DONATED
	bodyDonated, _ := json.Marshal(map[string]string{"status": "DONATED"})
	reqDonated := httptest.NewRequest(http.MethodPut, "/v1/listings/"+item.ID+"/status", bytes.NewReader(bodyDonated))
	reqDonated.Header.Set("Authorization", "Bearer mock-token-student-seller-1")
	recDonated := httptest.NewRecorder()
	router.ServeHTTP(recDonated, reqDonated)
	if recDonated.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for owner update to DONATED, got %d: %s", recDonated.Code, recDonated.Body.String())
	}

	updatedItem2, _ := listingsService.GetListing(item.ID)
	if updatedItem2.Status != listings.StatusDonated {
		t.Errorf("expected status %s, got %s", listings.StatusDonated, updatedItem2.Status)
	}
}

