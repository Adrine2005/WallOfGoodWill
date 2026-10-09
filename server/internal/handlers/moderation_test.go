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
	"goodwill-server/internal/moderation"
	"goodwill-server/internal/users"
)

func setupModerationTestEnv() (http.Handler, *listings.Service, *moderation.Service, *users.Service, *auth.MockVerifier) {
	userService := users.NewService()
	listingsService := listings.NewService()
	moderationService := moderation.NewService(listingsService)
	verifier := auth.NewMockVerifier()

	u1 := &users.User{
		UID:         "user-seller",
		Name:        "Seller Student",
		Email:       "seller@campus.edu",
		HostelBlock: "Hostel B",
		RoomNo:      "101",
		Verified:    true,
	}
	userService.SaveUser(u1)

	u2 := &users.User{
		UID:         "user-reporter-1",
		Name:        "Reporter Student 1",
		Email:       "rep1@campus.edu",
		HostelBlock: "Hostel A",
		RoomNo:      "201",
		Verified:    true,
	}
	userService.SaveUser(u2)

	u3 := &users.User{
		UID:         "user-reporter-2",
		Name:        "Reporter Student 2",
		Email:       "rep2@campus.edu",
		HostelBlock: "Hostel C",
		RoomNo:      "301",
		Verified:    true,
	}
	userService.SaveUser(u3)

	moderationHandler := handlers.NewModerationHandler(moderationService)
	listingsHandler := handlers.NewListingsHandler(listingsService, userService)

	r := chi.NewRouter()
	r.Route("/v1/listings", func(r chi.Router) {
		r.Get("/", listingsHandler.ListListings)
		r.Group(func(r chi.Router) {
			r.Use(auth.Middleware(verifier))
			r.Post("/{id}/reports", moderationHandler.SubmitReport)
			r.Get("/{id}/reports", moderationHandler.GetListingReports)
		})
	})

	return r, listingsService, moderationService, userService, verifier
}

func TestModeration_SubmitReport_Success(t *testing.T) {
	router, listingsService, _, _, _ := setupModerationTestEnv()

	item, err := listingsService.CreateListing(
		"user-seller", "Seller Student", "Hostel B", "101",
		"Suspicious Chemistry Compound", "Found in lab storage",
		listings.CategoryOther, listings.TypeDonate, 0, listings.ConditionFair, nil,
	)
	if err != nil {
		t.Fatalf("failed to create listing: %v", err)
	}

	body, _ := json.Marshal(handlers.CreateReportRequest{
		Reason:  moderation.ReasonProhibitedItem,
		Comment: "Hazardous campus laboratory compound prohibited in hostels.",
	})

	req := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/reports", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-user-reporter-1")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created, got %d: %s", w.Code, w.Body.String())
	}

	var rep moderation.Report
	if err := json.Unmarshal(w.Body.Bytes(), &rep); err != nil {
		t.Fatalf("failed to decode report: %v", err)
	}

	if rep.ListingID != item.ID || rep.ReporterID != "user-reporter-1" || rep.Reason != moderation.ReasonProhibitedItem {
		t.Errorf("unexpected report output: %+v", rep)
	}
}

func TestModeration_SelfReporting_Rejected(t *testing.T) {
	router, listingsService, _, _, _ := setupModerationTestEnv()

	item, _ := listingsService.CreateListing(
		"user-seller", "Seller Student", "Hostel B", "101",
		"Chemistry Textbook", "Reference book",
		listings.CategoryBooks, listings.TypeDonate, 0, listings.ConditionGood, nil,
	)

	body, _ := json.Marshal(handlers.CreateReportRequest{
		Reason:  moderation.ReasonSpamOrScam,
		Comment: "Self reporting test",
	})

	req := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/reports", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-user-seller")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusForbidden {
		t.Fatalf("expected 403 Forbidden for self reporting, got %d: %s", w.Code, w.Body.String())
	}
}

func TestModeration_InvalidReason_Rejected(t *testing.T) {
	router, listingsService, _, _, _ := setupModerationTestEnv()

	item, _ := listingsService.CreateListing(
		"user-seller", "Seller Student", "Hostel B", "101",
		"Chemistry Textbook", "Reference book",
		listings.CategoryBooks, listings.TypeDonate, 0, listings.ConditionGood, nil,
	)

	body, _ := json.Marshal(map[string]string{
		"reason":  "INVALID_UNKNOWN_REASON",
		"comment": "random text",
	})

	req := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/reports", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-user-reporter-1")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected 400 Bad Request for invalid reason, got %d", w.Code)
	}
}

func TestModeration_AutoConcealment_HidesListingFromFeed(t *testing.T) {
	router, listingsService, _, _, _ := setupModerationTestEnv()

	item, _ := listingsService.CreateListing(
		"user-seller", "Seller Student", "Hostel B", "101",
		"Counterfeit Electronics Gadget", "Fake item",
		listings.CategoryElectronics, listings.TypeSell, 400, listings.ConditionGood, nil,
	)

	// First report by reporter 1
	body1, _ := json.Marshal(handlers.CreateReportRequest{
		Reason:  moderation.ReasonSpamOrScam,
		Comment: "Suspicious fake item",
	})
	req1 := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/reports", bytes.NewReader(body1))
	req1.Header.Set("Authorization", "Bearer mock-token-user-reporter-1")
	w1 := httptest.NewRecorder()
	router.ServeHTTP(w1, req1)
	if w1.Code != http.StatusCreated {
		t.Fatalf("report 1 failed: %d", w1.Code)
	}

	// Listing should still be active
	activeList := listingsService.ListListings(listings.Filter{})
	found := false
	for _, l := range activeList {
		if l.ID == item.ID {
			found = true
			break
		}
	}
	if !found {
		t.Fatalf("listing should still be visible after 1 report")
	}

	// Second report by reporter 2 -> crosses threshold (2 reports)
	body2, _ := json.Marshal(handlers.CreateReportRequest{
		Reason:  moderation.ReasonSpamOrScam,
		Comment: "Confirmed fake seller",
	})
	req2 := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/reports", bytes.NewReader(body2))
	req2.Header.Set("Authorization", "Bearer mock-token-user-reporter-2")
	w2 := httptest.NewRecorder()
	router.ServeHTTP(w2, req2)
	if w2.Code != http.StatusCreated {
		t.Fatalf("report 2 failed: %d", w2.Code)
	}

	// Listing should now be HIDDEN and excluded from ListListings
	activeListAfter := listingsService.ListListings(listings.Filter{})
	for _, l := range activeListAfter {
		if l.ID == item.ID {
			t.Fatalf("listing should be concealed from feed after threshold reached!")
		}
	}

	// Fetch report audit logs
	getReq := httptest.NewRequest(http.MethodGet, "/v1/listings/"+item.ID+"/reports", nil)
	getReq.Header.Set("Authorization", "Bearer mock-token-user-reporter-1")
	getW := httptest.NewRecorder()
	router.ServeHTTP(getW, getReq)

	if getW.Code != http.StatusOK {
		t.Fatalf("expected 200 OK on audit fetch, got %d", getW.Code)
	}

	var logs []*moderation.Report
	if err := json.Unmarshal(getW.Body.Bytes(), &logs); err != nil {
		t.Fatalf("failed to decode audit logs: %v", err)
	}
	if len(logs) != 2 {
		t.Fatalf("expected 2 report audit entries, got %d", len(logs))
	}
}
