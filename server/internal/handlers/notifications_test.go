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
	"goodwill-server/internal/requests"
	"goodwill-server/internal/users"
)

func setupNotificationsEnv() (http.Handler, *notifications.Service, *requests.Service, *listings.Service, *users.Service, *auth.MockVerifier) {
	userService := users.NewService()
	listingsService := listings.NewService()
	notifService := notifications.NewService()
	requestsService := requests.NewService(listingsService, userService, notifService)
	verifier := auth.NewMockVerifier()

	donor := &users.User{
		UID:         "donor-notif",
		Name:        "Donor Student",
		Email:       "donor@campus.edu",
		HostelBlock: "Hostel A",
		RoomNo:      "101",
		Verified:    true,
	}
	userService.SaveUser(donor)

	requester := &users.User{
		UID:         "requester-notif",
		Name:        "Requester Student",
		Email:       "req@campus.edu",
		HostelBlock: "Hostel B",
		RoomNo:      "202",
		Verified:    true,
	}
	userService.SaveUser(requester)

	notifHandler := handlers.NewNotificationsHandler(notifService)
	requestsHandler := handlers.NewRequestsHandler(requestsService)

	r := chi.NewRouter()
	r.Route("/v1/notifications", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Get("/", notifHandler.GetNotifications)
		r.Get("/unread-count", notifHandler.GetUnreadCount)
		r.Post("/{id}/read", notifHandler.MarkAsRead)
		r.Post("/read-all", notifHandler.MarkAllAsRead)
	})

	r.Route("/v1/listings", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Post("/{id}/requests", requestsHandler.CreateRequest)
	})

	r.Route("/v1/requests", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Post("/{id}/accept", requestsHandler.AcceptRequest)
		r.Post("/{id}/decline", requestsHandler.DeclineRequest)
	})

	return r, notifService, requestsService, listingsService, userService, verifier
}

func TestNotifications_ListAndUnreadCount(t *testing.T) {
	router, notifService, _, _, _, _ := setupNotificationsEnv()

	// Initial empty state
	req := httptest.NewRequest(http.MethodGet, "/v1/notifications", nil)
	req.Header.Set("Authorization", "Bearer mock-token-donor-notif")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK, got %d", rec.Code)
	}

	var list []*notifications.Notification
	_ = json.NewDecoder(rec.Body).Decode(&list)
	if len(list) != 0 {
		t.Fatalf("expected 0 notifications, got %d", len(list))
	}

	// Create 2 notifications
	notifService.CreateNotification("donor-notif", "Alert 1", "Message 1", notifications.TypeRequestReceived, "item-1")
	notifService.CreateNotification("donor-notif", "Alert 2", "Message 2", notifications.TypeRequestReceived, "item-2")

	// Check unread count
	reqCount := httptest.NewRequest(http.MethodGet, "/v1/notifications/unread-count", nil)
	reqCount.Header.Set("Authorization", "Bearer mock-token-donor-notif")
	recCount := httptest.NewRecorder()
	router.ServeHTTP(recCount, reqCount)

	if recCount.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for count, got %d", recCount.Code)
	}

	var countRes map[string]int
	_ = json.NewDecoder(recCount.Body).Decode(&countRes)
	if countRes["count"] != 2 {
		t.Fatalf("expected unread count 2, got %d", countRes["count"])
	}
}

func TestNotifications_MarkAsReadAndMarkAllAsRead(t *testing.T) {
	router, notifService, _, _, _, _ := setupNotificationsEnv()

	n1 := notifService.CreateNotification("donor-notif", "Alert 1", "Msg 1", notifications.TypeRequestReceived, "t-1")
	n2 := notifService.CreateNotification("donor-notif", "Alert 2", "Msg 2", notifications.TypeRequestReceived, "t-2")

	// Mark single notification read
	reqRead := httptest.NewRequest(http.MethodPost, "/v1/notifications/"+n1.ID+"/read", nil)
	reqRead.Header.Set("Authorization", "Bearer mock-token-donor-notif")
	recRead := httptest.NewRecorder()
	router.ServeHTTP(recRead, reqRead)

	if recRead.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for mark read, got %d", recRead.Code)
	}

	var updated notifications.Notification
	_ = json.NewDecoder(recRead.Body).Decode(&updated)
	if !updated.IsRead {
		t.Errorf("expected isRead to be true")
	}

	if notifService.GetUnreadCount("donor-notif") != 1 {
		t.Errorf("expected 1 unread notification, got %d", notifService.GetUnreadCount("donor-notif"))
	}

	// Mark all read
	reqAll := httptest.NewRequest(http.MethodPost, "/v1/notifications/read-all", nil)
	reqAll.Header.Set("Authorization", "Bearer mock-token-donor-notif")
	recAll := httptest.NewRecorder()
	router.ServeHTTP(recAll, reqAll)

	if recAll.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for mark all read, got %d", recAll.Code)
	}

	if notifService.GetUnreadCount("donor-notif") != 0 {
		t.Errorf("expected 0 unread notifications after mark all, got %d", notifService.GetUnreadCount("donor-notif"))
	}
	_ = n2
}

func TestNotifications_RequestsTriggerFlow(t *testing.T) {
	_, notifService, requestsService, listingsService, _, _ := setupNotificationsEnv()

	item, err := listingsService.CreateListing(
		"donor-notif", "Donor Student", "Hostel A", "101",
		"Lab Thermometer", "Scientific thermometer",
		listings.CategoryOther, listings.TypeDonate, 0, listings.ConditionNew, nil,
	)
	if err != nil {
		t.Fatalf("failed to create listing: %v", err)
	}

	// Requester 1 creates request -> donor should get notification
	r1, err := requestsService.CreateRequest(item.ID, "requester-notif", "Need it for biology lab")
	if err != nil {
		t.Fatalf("failed to create request: %v", err)
	}

	donorNotifs := notifService.GetNotifications("donor-notif")
	if len(donorNotifs) != 1 {
		t.Fatalf("expected 1 notification for donor, got %d", len(donorNotifs))
	}
	if donorNotifs[0].Type != notifications.TypeRequestReceived {
		t.Errorf("expected TypeRequestReceived, got %s", donorNotifs[0].Type)
	}

	// Requester 2 creates competing request
	r2, _ := requestsService.CreateRequest(item.ID, "competitor-notif", "Need it as well")

	// Donor accepts r1 -> r1 requester gets ACCEPTED, r2 competitor gets DECLINED
	_, err = requestsService.AcceptRequest(r1.ID, "donor-notif")
	if err != nil {
		t.Fatalf("failed to accept request: %v", err)
	}

	req1Notifs := notifService.GetNotifications("requester-notif")
	if len(req1Notifs) != 1 || req1Notifs[0].Type != notifications.TypeRequestAccepted {
		t.Fatalf("expected TypeRequestAccepted for accepted requester, got %+v", req1Notifs)
	}

	compNotifs := notifService.GetNotifications("competitor-notif")
	if len(compNotifs) != 1 || compNotifs[0].Type != notifications.TypeRequestDeclined {
		t.Fatalf("expected TypeRequestDeclined for competitor, got %+v", compNotifs)
	}
	_ = r2
}
