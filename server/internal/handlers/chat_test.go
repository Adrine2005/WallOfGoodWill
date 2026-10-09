package handlers_test

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/chat"
	"goodwill-server/internal/handlers"
	"goodwill-server/internal/listings"
	"goodwill-server/internal/notifications"
	"goodwill-server/internal/users"
)

func setupChatEnv() (http.Handler, *chat.Service, *listings.Service, *users.Service, *notifications.Service, *auth.MockVerifier) {
	userService := users.NewService()
	listingsService := listings.NewService()
	notifService := notifications.NewService()
	chatService := chat.NewService(listingsService, userService, notifService)
	verifier := auth.NewMockVerifier()

	seller := &users.User{
		UID:         "seller-user",
		Name:        "Seller Student",
		Email:       "seller@campus.edu",
		HostelBlock: "Hostel A",
		RoomNo:      "101",
		Verified:    true,
	}
	userService.SaveUser(seller)

	buyer := &users.User{
		UID:         "buyer-user",
		Name:        "Buyer Student",
		Email:       "buyer@campus.edu",
		HostelBlock: "Hostel B",
		RoomNo:      "202",
		Verified:    true,
	}
	userService.SaveUser(buyer)

	otherUser := &users.User{
		UID:         "intruder-user",
		Name:        "Intruder Student",
		Email:       "intruder@campus.edu",
		HostelBlock: "Hostel C",
		RoomNo:      "303",
		Verified:    true,
	}
	userService.SaveUser(otherUser)

	chatHandler := handlers.NewChatHandler(chatService)

	r := chi.NewRouter()
	r.Route("/v1/chat", func(r chi.Router) {
		r.Use(auth.Middleware(verifier))
		r.Post("/conversations", chatHandler.CreateOrGetConversation)
		r.Get("/conversations", chatHandler.GetConversations)
		r.Get("/conversations/{id}", chatHandler.GetConversation)
		r.Get("/conversations/{id}/messages", chatHandler.GetMessages)
		r.Post("/conversations/{id}/messages", chatHandler.SendMessage)
	})

	return r, chatService, listingsService, userService, notifService, verifier
}

func TestChat_CreateOrGetConversation_Deduplication(t *testing.T) {
	router, _, listingsService, _, _, _ := setupChatEnv()

	item, err := listingsService.CreateListing(
		"seller-user", "Seller Student", "Hostel A", "101",
		"Calculus Textbook", "Clean condition 9th edition",
		listings.CategoryBooks, listings.TypeSell, 350, listings.ConditionGood, nil,
	)
	if err != nil {
		t.Fatalf("failed to create listing: %v", err)
	}

	// 1. Buyer initiates conversation
	body, _ := json.Marshal(handlers.CreateConversationRequest{ListingID: item.ID})
	req := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-buyer-user")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK, got %d: %s", rec.Code, rec.Body.String())
	}

	var conv1 chat.Conversation
	if err := json.NewDecoder(rec.Body).Decode(&conv1); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}

	if conv1.ID == "" || conv1.ListingID != item.ID || conv1.BuyerID != "buyer-user" || conv1.SellerID != "seller-user" {
		t.Fatalf("unexpected conversation details: %+v", conv1)
	}

	// 2. Buyer initiates conversation again on the same listing -> must return same ID (deduplicated)
	req2 := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations", bytes.NewReader(body))
	req2.Header.Set("Authorization", "Bearer mock-token-buyer-user")
	rec2 := httptest.NewRecorder()
	router.ServeHTTP(rec2, req2)

	if rec2.Code != http.StatusOK {
		t.Fatalf("expected 200 OK on deduplicated request, got %d", rec2.Code)
	}

	var conv2 chat.Conversation
	_ = json.NewDecoder(rec2.Body).Decode(&conv2)
	if conv2.ID != conv1.ID {
		t.Fatalf("expected duplicate conversation ID %s, got %s", conv1.ID, conv2.ID)
	}

	// 3. Seller cannot initiate conversation on their own listing
	reqSelf := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations", bytes.NewReader(body))
	reqSelf.Header.Set("Authorization", "Bearer mock-token-seller-user")
	recSelf := httptest.NewRecorder()
	router.ServeHTTP(recSelf, reqSelf)

	if recSelf.Code != http.StatusBadRequest {
		t.Fatalf("expected 400 Bad Request for self chat, got %d", recSelf.Code)
	}
}

func TestChat_SendMessage_And_Notification(t *testing.T) {
	router, _, listingsService, _, notifService, _ := setupChatEnv()

	item, _ := listingsService.CreateListing(
		"seller-user", "Seller Student", "Hostel A", "101",
		"Table Lamp", "LED study lamp",
		listings.CategoryFurniture, listings.TypeDonate, 0, listings.ConditionLikeNew, nil,
	)

	// Create conversation
	body, _ := json.Marshal(handlers.CreateConversationRequest{ListingID: item.ID})
	req := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-buyer-user")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	var conv chat.Conversation
	_ = json.NewDecoder(rec.Body).Decode(&conv)

	// Buyer sends message
	msgBody, _ := json.Marshal(handlers.SendMessageRequest{Text: "Hi! Can we meet at Hostel A mess today at 5pm?"})
	msgReq := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations/"+conv.ID+"/messages", bytes.NewReader(msgBody))
	msgReq.Header.Set("Authorization", "Bearer mock-token-buyer-user")
	msgRec := httptest.NewRecorder()
	router.ServeHTTP(msgRec, msgReq)

	if msgRec.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created, got %d: %s", msgRec.Code, msgRec.Body.String())
	}

	var sentMsg chat.ChatMessage
	_ = json.NewDecoder(msgRec.Body).Decode(&sentMsg)
	if sentMsg.Text != "Hi! Can we meet at Hostel A mess today at 5pm?" || sentMsg.SenderID != "buyer-user" {
		t.Fatalf("unexpected message payload: %+v", sentMsg)
	}

	// Verify seller received in-app notification
	sellerNotifs := notifService.GetNotifications("seller-user")
	if len(sellerNotifs) != 1 {
		t.Fatalf("expected 1 notification for seller, got %d", len(sellerNotifs))
	}
	if sellerNotifs[0].Type != notifications.TypeChatMessage {
		t.Errorf("expected TypeChatMessage, got %s", sellerNotifs[0].Type)
	}
	if sellerNotifs[0].TargetID != conv.ID {
		t.Errorf("expected notification target ID %s, got %s", conv.ID, sellerNotifs[0].TargetID)
	}

	// Seller replies
	replyBody, _ := json.Marshal(handlers.SendMessageRequest{Text: "Yes, 5pm works perfectly for me!"})
	replyReq := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations/"+conv.ID+"/messages", bytes.NewReader(replyBody))
	replyReq.Header.Set("Authorization", "Bearer mock-token-seller-user")
	replyRec := httptest.NewRecorder()
	router.ServeHTTP(replyRec, replyReq)

	if replyRec.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created for reply, got %d", replyRec.Code)
	}

	// Buyer gets notification
	buyerNotifs := notifService.GetNotifications("buyer-user")
	if len(buyerNotifs) != 1 {
		t.Fatalf("expected 1 notification for buyer, got %d", len(buyerNotifs))
	}

	// Fetch message history
	histReq := httptest.NewRequest(http.MethodGet, "/v1/chat/conversations/"+conv.ID+"/messages", nil)
	histReq.Header.Set("Authorization", "Bearer mock-token-buyer-user")
	histRec := httptest.NewRecorder()
	router.ServeHTTP(histRec, histReq)

	if histRec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for messages, got %d", histRec.Code)
	}

	var messages []*chat.ChatMessage
	_ = json.NewDecoder(histRec.Body).Decode(&messages)
	if len(messages) != 2 {
		t.Fatalf("expected 2 messages in conversation, got %d", len(messages))
	}
	if messages[0].Text != "Hi! Can we meet at Hostel A mess today at 5pm?" || messages[1].Text != "Yes, 5pm works perfectly for me!" {
		t.Errorf("unexpected message order or content: %+v", messages)
	}
}

func TestChat_UnauthorizedAccess(t *testing.T) {
	router, _, listingsService, _, _, _ := setupChatEnv()

	item, _ := listingsService.CreateListing(
		"seller-user", "Seller Student", "Hostel A", "101",
		"Bicycle Pump", "Portable tire pump",
		listings.CategoryCycles, listings.TypeDonate, 0, listings.ConditionFair, nil,
	)

	body, _ := json.Marshal(handlers.CreateConversationRequest{ListingID: item.ID})
	req := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations", bytes.NewReader(body))
	req.Header.Set("Authorization", "Bearer mock-token-buyer-user")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	var conv chat.Conversation
	_ = json.NewDecoder(rec.Body).Decode(&conv)

	// Intruder tries to read messages
	intruderReq := httptest.NewRequest(http.MethodGet, "/v1/chat/conversations/"+conv.ID+"/messages", nil)
	intruderReq.Header.Set("Authorization", "Bearer mock-token-intruder-user")
	intruderRec := httptest.NewRecorder()
	router.ServeHTTP(intruderRec, intruderReq)

	if intruderRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 Forbidden for non-participant, got %d", intruderRec.Code)
	}

	// Intruder tries to send message
	sendBody, _ := json.Marshal(handlers.SendMessageRequest{Text: "Unauthorized payload"})
	intruderSend := httptest.NewRequest(http.MethodPost, "/v1/chat/conversations/"+conv.ID+"/messages", bytes.NewReader(sendBody))
	intruderSend.Header.Set("Authorization", "Bearer mock-token-intruder-user")
	intruderSendRec := httptest.NewRecorder()
	router.ServeHTTP(intruderSendRec, intruderSend)

	if intruderSendRec.Code != http.StatusForbidden {
		t.Fatalf("expected 403 Forbidden for non-participant sending, got %d", intruderSendRec.Code)
	}
}
