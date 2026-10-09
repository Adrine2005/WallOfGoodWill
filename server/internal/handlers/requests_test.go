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
	"goodwill-server/internal/requests"
	"goodwill-server/internal/users"
)

func setupRequestsEnv() (http.Handler, *listings.Service, *requests.Service, *users.Service, *auth.MockVerifier) {
	userService := users.NewService()
	listingsService := listings.NewService()
	requestsService := requests.NewService(listingsService, userService)
	verifier := auth.NewMockVerifier()

	// Donor user
	donor := &users.User{
		UID:         "donor-1",
		Name:        "Donor Student",
		Email:       "donor@campus.edu",
		HostelBlock: "Hostel A",
		RoomNo:      "101",
		Verified:    true,
	}
	userService.SaveUser(donor)

	// Requester 1
	requester1 := &users.User{
		UID:         "requester-1",
		Name:        "Requester One",
		Email:       "req1@campus.edu",
		HostelBlock: "Hostel B",
		RoomNo:      "202",
		Verified:    true,
	}
	userService.SaveUser(requester1)

	// Requester 2
	requester2 := &users.User{
		UID:         "requester-2",
		Name:        "Requester Two",
		Email:       "req2@campus.edu",
		HostelBlock: "Hostel C",
		RoomNo:      "303",
		Verified:    true,
	}
	userService.SaveUser(requester2)

	listingsHandler := handlers.NewListingsHandler(listingsService, userService)
	requestsHandler := handlers.NewRequestsHandler(requestsService)

	r := chi.NewRouter()
	r.Route("/v1/listings", func(r chi.Router) {
		r.Get("/", listingsHandler.ListListings)
		r.Get("/{id}", listingsHandler.GetListing)

		r.Group(func(r chi.Router) {
			r.Use(auth.Middleware(verifier))
			r.Post("/", listingsHandler.CreateListing)
			r.Post("/{id}/requests", requestsHandler.CreateRequest)
			r.Get("/{id}/requests", requestsHandler.GetListingRequests)
		})
	})

	r.Route("/v1/requests", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Get("/incoming", requestsHandler.GetIncomingRequests)
		r.Post("/{id}/accept", requestsHandler.AcceptRequest)
		r.Post("/{id}/decline", requestsHandler.DeclineRequest)
	})

	return r, listingsService, requestsService, userService, verifier
}

func TestCreateDonationRequest_Success(t *testing.T) {
	router, listingsService, _, _, _ := setupRequestsEnv()

	// Create a donation item owned by donor-1
	item, err := listingsService.CreateListing(
		"donor-1", "Donor Student", "Hostel A", "101",
		"Engineering Graphics Drafter", "Barely used mini drafter",
		listings.CategoryStationery, listings.TypeDonate, 0, listings.ConditionLikeNew, nil,
	)
	if err != nil {
		t.Fatalf("failed to create listing: %v", err)
	}

	payload := map[string]string{
		"message": "Hey, I need this for my first year graphics course!",
	}
	body, _ := json.Marshal(payload)

	req := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/requests", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-requester-1")
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected status 201 Created, got %d: %s", rec.Code, rec.Body.String())
	}

	var res requests.DonationRequest
	if err := json.NewDecoder(rec.Body).Decode(&res); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}

	if res.ListingID != item.ID {
		t.Errorf("expected listing ID %s, got %s", item.ID, res.ListingID)
	}
	if res.RequesterID != "requester-1" {
		t.Errorf("expected requester ID requester-1, got %s", res.RequesterID)
	}
	if res.Status != requests.StatusPending {
		t.Errorf("expected status PENDING, got %s", res.Status)
	}
	if res.Message != payload["message"] {
		t.Errorf("expected message %s, got %s", payload["message"], res.Message)
	}
}

func TestCreateDonationRequest_SelfRequestRejected(t *testing.T) {
	router, listingsService, _, _, _ := setupRequestsEnv()

	item, _ := listingsService.CreateListing(
		"donor-1", "Donor Student", "Hostel A", "101",
		"Scientific Calculator", "Casio fx-991EX",
		listings.CategoryElectronics, listings.TypeDonate, 0, listings.ConditionGood, nil,
	)

	payload := map[string]string{
		"message": "Requesting my own item",
	}
	body, _ := json.Marshal(payload)

	req := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/requests", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-donor-1")
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected status 400 Bad Request for self-request, got %d", rec.Code)
	}
}

func TestCreateDonationRequest_NonDonationRejected(t *testing.T) {
	router, listingsService, _, _, _ := setupRequestsEnv()

	// Sell item
	item, _ := listingsService.CreateListing(
		"donor-1", "Donor Student", "Hostel A", "101",
		"Mechanical Keyboard", "Keychron K2",
		listings.CategoryElectronics, listings.TypeSell, 2500, listings.ConditionGood, nil,
	)

	payload := map[string]string{
		"message": "Can I have this for free?",
	}
	body, _ := json.Marshal(payload)

	req := httptest.NewRequest(http.MethodPost, "/v1/listings/"+item.ID+"/requests", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-requester-1")
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected status 400 Bad Request for sell item, got %d", rec.Code)
	}
}

func TestGetListingRequests_Authorization(t *testing.T) {
	router, listingsService, requestsService, _, _ := setupRequestsEnv()

	item, _ := listingsService.CreateListing(
		"donor-1", "Donor Student", "Hostel A", "101",
		"Lab Coat", "White lab coat size M",
		listings.CategoryStationery, listings.TypeDonate, 0, listings.ConditionGood, nil,
	)

	_, _ = requestsService.CreateRequest(item.ID, "requester-1", "Need for chemistry lab")

	// Donor checks requests -> OK
	reqDonor := httptest.NewRequest(http.MethodGet, "/v1/listings/"+item.ID+"/requests", nil)
	reqDonor.Header.Set("Authorization", "Bearer mock-token-donor-1")
	recDonor := httptest.NewRecorder()
	router.ServeHTTP(recDonor, reqDonor)

	if recDonor.Code != http.StatusOK {
		t.Fatalf("expected status 200 for donor, got %d", recDonor.Code)
	}

	var list []*requests.DonationRequest
	_ = json.NewDecoder(recDonor.Body).Decode(&list)
	if len(list) != 1 {
		t.Fatalf("expected 1 request, got %d", len(list))
	}

	// Another user checks requests -> 403 Forbidden
	reqOther := httptest.NewRequest(http.MethodGet, "/v1/listings/"+item.ID+"/requests", nil)
	reqOther.Header.Set("Authorization", "Bearer mock-token-requester-1")
	recOther := httptest.NewRecorder()
	router.ServeHTTP(recOther, reqOther)

	if recOther.Code != http.StatusForbidden {
		t.Fatalf("expected status 403 Forbidden for non-donor, got %d", recOther.Code)
	}
}

func TestGetIncomingRequests_Donor(t *testing.T) {
	router, listingsService, requestsService, _, _ := setupRequestsEnv()

	item, _ := listingsService.CreateListing(
		"donor-1", "Donor Student", "Hostel A", "101",
		"Physics Textbook", "Halliday & Resnick",
		listings.CategoryBooks, listings.TypeDonate, 0, listings.ConditionFair, nil,
	)

	_, _ = requestsService.CreateRequest(item.ID, "requester-1", "Please give me this book")

	req := httptest.NewRequest(http.MethodGet, "/v1/requests/incoming", nil)
	req.Header.Set("Authorization", "Bearer mock-token-donor-1")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK, got %d", rec.Code)
	}

	var list []*requests.DonationRequest
	_ = json.NewDecoder(rec.Body).Decode(&list)
	if len(list) != 1 {
		t.Fatalf("expected 1 incoming request, got %d", len(list))
	}
}

func TestAcceptRequest_AutoDeclinesCompetitorsAndMarksDonated(t *testing.T) {
	router, listingsService, requestsService, _, _ := setupRequestsEnv()

	item, _ := listingsService.CreateListing(
		"donor-1", "Donor Student", "Hostel A", "101",
		"Data Structures Notes", "Complete handwritten notes",
		listings.CategoryBooks, listings.TypeDonate, 0, listings.ConditionLikeNew, nil,
	)

	r1, err1 := requestsService.CreateRequest(item.ID, "requester-1", "I really need these notes!")
	if err1 != nil {
		t.Fatalf("failed to create r1: %v", err1)
	}
	r2, err2 := requestsService.CreateRequest(item.ID, "requester-2", "Can I take them?")
	if err2 != nil {
		t.Fatalf("failed to create r2: %v", err2)
	}

	// Donor accepts r1
	req := httptest.NewRequest(http.MethodPost, "/v1/requests/"+r1.ID+"/accept", nil)
	req.Header.Set("Authorization", "Bearer mock-token-donor-1")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK on accept, got %d: %s", rec.Code, rec.Body.String())
	}

	var accepted requests.DonationRequest
	_ = json.NewDecoder(rec.Body).Decode(&accepted)
	if accepted.Status != requests.StatusAccepted {
		t.Errorf("expected accepted status ACCEPTED, got %s", accepted.Status)
	}

	// Verify listing status is now DONATED
	updatedListing, err := listingsService.GetListing(item.ID)
	if err != nil {
		t.Fatalf("failed to get listing: %v", err)
	}
	if updatedListing.Status != listings.StatusDonated {
		t.Errorf("expected listing status DONATED, got %s", updatedListing.Status)
	}

	// Verify competitor r2 is automatically DECLINED
	donorRequests, _ := requestsService.GetListingRequests(item.ID, "donor-1")
	var r2Found *requests.DonationRequest
	for _, r := range donorRequests {
		if r.ID == r2.ID {
			r2Found = r
			break
		}
	}
	if r2Found == nil || r2Found.Status != requests.StatusDeclined {
		t.Errorf("expected competing request r2 to be DECLINED, got %+v", r2Found)
	}

	// Second attempt to accept already resolved request should fail
	recAgain := httptest.NewRecorder()
	router.ServeHTTP(recAgain, req)
	if recAgain.Code != http.StatusBadRequest {
		t.Fatalf("expected 400 Bad Request on already resolved request, got %d", recAgain.Code)
	}
}

func TestDeclineRequest_Success(t *testing.T) {
	router, listingsService, requestsService, _, _ := setupRequestsEnv()

	item, _ := listingsService.CreateListing(
		"donor-1", "Donor Student", "Hostel A", "101",
		"Cycle Pump", "Portable tire pump",
		listings.CategoryCycles, listings.TypeDonate, 0, listings.ConditionGood, nil,
	)

	r1, _ := requestsService.CreateRequest(item.ID, "requester-1", "Can I take it?")

	// Unauthorized non-donor decline
	reqUnauthorized := httptest.NewRequest(http.MethodPost, "/v1/requests/"+r1.ID+"/decline", nil)
	reqUnauthorized.Header.Set("Authorization", "Bearer mock-token-requester-2")
	recUnauthorized := httptest.NewRecorder()
	router.ServeHTTP(recUnauthorized, reqUnauthorized)
	if recUnauthorized.Code != http.StatusForbidden {
		t.Fatalf("expected 403 Forbidden for unauthorized user, got %d", recUnauthorized.Code)
	}

	// Donor decline
	req := httptest.NewRequest(http.MethodPost, "/v1/requests/"+r1.ID+"/decline", nil)
	req.Header.Set("Authorization", "Bearer mock-token-donor-1")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK on decline, got %d: %s", rec.Code, rec.Body.String())
	}

	var declined requests.DonationRequest
	_ = json.NewDecoder(rec.Body).Decode(&declined)
	if declined.Status != requests.StatusDeclined {
		t.Errorf("expected DECLINED, got %s", declined.Status)
	}
}
