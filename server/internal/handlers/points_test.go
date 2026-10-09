package handlers_test

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/handlers"
	"goodwill-server/internal/listings"
	"goodwill-server/internal/notifications"
	"goodwill-server/internal/points"
	"goodwill-server/internal/requests"
	"goodwill-server/internal/users"
)

func setupPointsTestEnv() (http.Handler, *points.Service, *users.Service, *listings.Service, *requests.Service, *auth.MockVerifier) {
	userService := users.NewService()
	listingsService := listings.NewService()
	notifService := notifications.NewService()
	requestsService := requests.NewService(listingsService, userService, notifService)
	pointsService := points.NewService(userService)
	requestsService.SetPointsService(pointsService)
	verifier := auth.NewMockVerifier()

	u1 := &users.User{
		UID:         "user-donor-test",
		Name:        "Test Donor",
		Email:       "donor@campus.edu",
		HostelBlock: "Hostel C",
		RoomNo:      "101",
		Verified:    true,
	}
	userService.SaveUser(u1)

	u2 := &users.User{
		UID:         "user-requester-test",
		Name:        "Test Requester",
		Email:       "requester@campus.edu",
		HostelBlock: "Hostel A",
		RoomNo:      "202",
		Verified:    true,
	}
	userService.SaveUser(u2)

	pointsHandler := handlers.NewPointsHandler(pointsService)

	r := chi.NewRouter()
	r.Route("/v1/points", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Get("/me", pointsHandler.GetMyPoints)
		r.Get("/leaderboard", pointsHandler.GetLeaderboard)
	})

	return r, pointsService, userService, listingsService, requestsService, verifier
}

func TestPoints_GetMyPoints_Newcomer(t *testing.T) {
	router, _, _, _, _, _ := setupPointsTestEnv()

	req := httptest.NewRequest(http.MethodGet, "/v1/points/me", nil)
	req.Header.Set("Authorization", "Bearer mock-token-user-donor-test")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200 OK, got %d: %s", w.Code, w.Body.String())
	}

	var res points.UserPoints
	if err := json.Unmarshal(w.Body.Bytes(), &res); err != nil {
		t.Fatalf("failed to decode JSON: %v", err)
	}

	if res.UserID != "user-donor-test" {
		t.Errorf("expected userID 'user-donor-test', got '%s'", res.UserID)
	}
	if res.Points != 0 {
		t.Errorf("expected 0 points for newcomer, got %d", res.Points)
	}
	if res.TierName != "Newcomer" {
		t.Errorf("expected TierName 'Newcomer', got '%s'", res.TierName)
	}
}

func TestPoints_AwardDonationPoints_ViaDonationAcceptance(t *testing.T) {
	router, pointsService, _, listingsService, requestsService, _ := setupPointsTestEnv()

	// Create a donation listing
	item, err := listingsService.CreateListing(
		"user-donor-test", "Test Donor", "Hostel C", "101",
		"Engineering Mathematics Book", "Good condition, second edition",
		listings.CategoryStationery, listings.TypeDonate, 0, listings.ConditionGood, nil,
	)
	if err != nil {
		t.Fatalf("failed to create listing: %v", err)
	}

	// Requester requests item
	donationReq, err := requestsService.CreateRequest(item.ID, "user-requester-test", "I really need this for class!")
	if err != nil {
		t.Fatalf("failed to create donation request: %v", err)
	}

	// Accept request -> should award 50 points to donor
	accepted, err := requestsService.AcceptRequest(donationReq.ID, "user-donor-test")
	if err != nil {
		t.Fatalf("failed to accept request: %v", err)
	}
	if accepted.Status != requests.StatusAccepted {
		t.Fatalf("expected accepted status, got %s", accepted.Status)
	}

	// Check points directly from service
	userPts := pointsService.GetUserPoints("user-donor-test")
	if userPts.Points != 50 {
		t.Errorf("expected 50 points after donation acceptance, got %d", userPts.Points)
	}
	if userPts.DonationCount != 1 {
		t.Errorf("expected 1 donation, got %d", userPts.DonationCount)
	}
	if userPts.TierName != "Donor Pioneer" {
		t.Errorf("expected 'Donor Pioneer' tier, got '%s'", userPts.TierName)
	}
	if len(userPts.Badges) < 1 || userPts.Badges[0].ID != "donor-pioneer" {
		t.Errorf("expected 'donor-pioneer' badge awarded")
	}

	// Check via HTTP endpoint
	req := httptest.NewRequest(http.MethodGet, "/v1/points/me", nil)
	req.Header.Set("Authorization", "Bearer mock-token-user-donor-test")
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d: %s", w.Code, w.Body.String())
	}
	var httpPts points.UserPoints
	if err := json.Unmarshal(w.Body.Bytes(), &httpPts); err != nil {
		t.Fatalf("failed to parse JSON: %v", err)
	}
	if httpPts.Points != 50 || httpPts.TierName != "Donor Pioneer" {
		t.Errorf("unexpected http response points: %+v", httpPts)
	}
}

func TestPoints_Leaderboard(t *testing.T) {
	router, _, _, _, _, _ := setupPointsTestEnv()

	req := httptest.NewRequest(http.MethodGet, "/v1/points/leaderboard", nil)
	req.Header.Set("Authorization", "Bearer mock-token-user-donor-test")
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200 OK, got %d: %s", w.Code, w.Body.String())
	}

	var leaderboard []*points.LeaderboardEntry
	if err := json.Unmarshal(w.Body.Bytes(), &leaderboard); err != nil {
		t.Fatalf("failed to decode leaderboard: %v", err)
	}

	if len(leaderboard) < 3 {
		t.Fatalf("expected at least 3 seed leaderboard entries, got %d", len(leaderboard))
	}

	// Verify ranks are ordered descending by points
	for i := 0; i < len(leaderboard)-1; i++ {
		if leaderboard[i].Points < leaderboard[i+1].Points {
			t.Errorf("leaderboard not sorted: rank %d (%d pts) < rank %d (%d pts)",
				leaderboard[i].Rank, leaderboard[i].Points,
				leaderboard[i+1].Rank, leaderboard[i+1].Points)
		}
		if leaderboard[i].Rank != i+1 {
			t.Errorf("rank expected %d, got %d", i+1, leaderboard[i].Rank)
		}
	}
}

func TestPoints_Unauthorized(t *testing.T) {
	router, _, _, _, _, _ := setupPointsTestEnv()

	req := httptest.NewRequest(http.MethodGet, "/v1/points/me", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 Unauthorized, got %d", w.Code)
	}
}
