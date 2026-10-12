package handlers_test

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/handlers"
	"goodwill-server/internal/invites"
	"goodwill-server/internal/users"
)

func setupTestEnv() (*handlers.AuthHandler, *handlers.AdminHandler, *users.Service, *invites.Service, *auth.MockVerifier) {
	userService := users.NewService()
	inviteService := invites.NewService()
	verifier := auth.NewMockVerifier()

	authHandler := handlers.NewAuthHandler(userService, inviteService, verifier)
	adminHandler := handlers.NewAdminHandler(inviteService)

	return authHandler, adminHandler, userService, inviteService, verifier
}

func TestRegister_Success_WithoutInvite(t *testing.T) {
	authHandler, _, userService, _, _ := setupTestEnv()

	body := map[string]string{
		"uid":         "student-001",
		"name":        "Aarav Sharma",
		"email":       "aarav@example.com",
		"hostelBlock": "Block A",
		"roomNo":      "302",
		"phone":       "9876543210",
	}
	bodyBytes, _ := json.Marshal(body)

	req := httptest.NewRequest(http.MethodPost, "/v1/auth/register", bytes.NewReader(bodyBytes))
	rec := httptest.NewRecorder()

	authHandler.Register(rec, req)

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected status %d, got %d. body: %s", http.StatusCreated, rec.Code, rec.Body.String())
	}

	var resp handlers.AuthResponse
	if err := json.NewDecoder(rec.Body).Decode(&resp); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}

	if resp.Verified {
		t.Errorf("expected verified to be false, got true")
	}

	saved, err := userService.GetUser("student-001")
	if err != nil {
		t.Fatalf("failed to find user in store: %v", err)
	}
	if saved.Name != "Aarav Sharma" || saved.HostelBlock != "Block A" {
		t.Errorf("unexpected user record: %+v", saved)
	}
}

func TestRegister_ValidationErrors(t *testing.T) {
	authHandler, _, _, _, _ := setupTestEnv()

	testCases := []struct {
		name string
		body map[string]string
	}{
		{"missing name", map[string]string{"uid": "u1", "email": "e@e.com", "hostelBlock": "A", "roomNo": "1"}},
		{"missing hostelBlock", map[string]string{"uid": "u1", "name": "N", "email": "e@e.com", "roomNo": "1"}},
		{"missing roomNo", map[string]string{"uid": "u1", "name": "N", "email": "e@e.com", "hostelBlock": "A"}},
		{"missing uid & claims", map[string]string{"name": "N", "email": "e@e.com", "hostelBlock": "A", "roomNo": "1"}},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			b, _ := json.Marshal(tc.body)
			req := httptest.NewRequest(http.MethodPost, "/v1/auth/register", bytes.NewReader(b))
			rec := httptest.NewRecorder()

			authHandler.Register(rec, req)

			if rec.Code != http.StatusBadRequest {
				t.Fatalf("expected status %d, got %d", http.StatusBadRequest, rec.Code)
			}
		})
	}
}

func TestRegister_WithInstantInviteCode(t *testing.T) {
	authHandler, _, userService, inviteService, verifier := setupTestEnv()

	invite, err := inviteService.CreateInvite("Hostel Notice Board", 2*time.Hour, 10)
	if err != nil {
		t.Fatalf("failed to create invite: %v", err)
	}

	body := map[string]string{
		"uid":         "student-instant",
		"name":        "Diya Patel",
		"email":       "diya@example.com",
		"hostelBlock": "Block C",
		"roomNo":      "104",
		"inviteCode":  invite.Code,
	}
	bodyBytes, _ := json.Marshal(body)

	req := httptest.NewRequest(http.MethodPost, "/v1/auth/register", bytes.NewReader(bodyBytes))
	rec := httptest.NewRecorder()

	authHandler.Register(rec, req)

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected status %d, got %d. body: %s", http.StatusCreated, rec.Code, rec.Body.String())
	}

	var resp handlers.AuthResponse
	_ = json.NewDecoder(rec.Body).Decode(&resp)

	if !resp.Verified {
		t.Errorf("expected user to be instantly verified")
	}

	saved, err := userService.GetUser("student-instant")
	if err != nil || !saved.Verified || saved.VerifiedViaCode != invite.Code {
		t.Errorf("expected saved user to be verified via code %s, got %+v", invite.Code, saved)
	}

	claims, err := verifier.VerifyToken(context.Background(), "mock-token-student-instant")
	if err != nil || !claims.Verified {
		t.Errorf("expected verifier claims to be verified")
	}
}

func TestRegister_WithInvalidOrExpiredCode(t *testing.T) {
	authHandler, _, _, inviteService, _ := setupTestEnv()

	// Non-existent code
	bodyNotFound := map[string]string{
		"uid":         "u-err1",
		"name":        "Test",
		"email":       "t@t.com",
		"hostelBlock": "B",
		"roomNo":      "101",
		"inviteCode":  "GW-INVALID",
	}
	b1, _ := json.Marshal(bodyNotFound)
	req1 := httptest.NewRequest(http.MethodPost, "/v1/auth/register", bytes.NewReader(b1))
	rec1 := httptest.NewRecorder()
	authHandler.Register(rec1, req1)
	if rec1.Code != http.StatusNotFound {
		t.Fatalf("expected 404 for invalid code, got %d", rec1.Code)
	}

	// Expired code
	expiredInvite, _ := inviteService.CreateInvite("Old Code", -10*time.Minute, 5)
	bodyExpired := map[string]string{
		"uid":         "u-err2",
		"name":        "Test",
		"email":       "t@t.com",
		"hostelBlock": "B",
		"roomNo":      "102",
		"inviteCode":  expiredInvite.Code,
	}
	b2, _ := json.Marshal(bodyExpired)
	req2 := httptest.NewRequest(http.MethodPost, "/v1/auth/register", bytes.NewReader(b2))
	rec2 := httptest.NewRecorder()
	authHandler.Register(rec2, req2)
	if rec2.Code != http.StatusBadRequest {
		t.Fatalf("expected 400 for expired code, got %d", rec2.Code)
	}
}

func TestVerifyQR_Lifecycle(t *testing.T) {
	authHandler, _, userService, inviteService, verifier := setupTestEnv()

	// Register unverified student first
	user := &users.User{
		UID:         "student-gateway",
		Name:        "Rohan Das",
		Email:       "rohan@example.com",
		HostelBlock: "Block D",
		RoomNo:      "415",
		Verified:    false,
	}
	userService.SaveUser(user)

	invite, err := inviteService.CreateInvite("Gateway Board", 1*time.Hour, 100)
	if err != nil {
		t.Fatalf("failed to create invite: %v", err)
	}

	// 1. Verify with valid unexpired QR code
	validReqBody, _ := json.Marshal(map[string]string{"code": invite.Code})
	req := httptest.NewRequest(http.MethodPost, "/v1/auth/verify-qr", bytes.NewReader(validReqBody))
	// Set mock claims on context
	ctxReq := auth.Middleware(verifier)(http.HandlerFunc(authHandler.VerifyQR))
	req.Header.Set("Authorization", "Bearer mock-token-student-gateway")
	rec := httptest.NewRecorder()

	ctxReq.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d. body: %s", http.StatusOK, rec.Code, rec.Body.String())
	}

	var verifyResp handlers.AuthResponse
	_ = json.NewDecoder(rec.Body).Decode(&verifyResp)
	if !verifyResp.Verified {
		t.Errorf("expected verification to succeed")
	}

	updatedUser, _ := userService.GetUser("student-gateway")
	if !updatedUser.Verified || updatedUser.VerifiedViaCode != invite.Code {
		t.Errorf("user not verified in store: %+v", updatedUser)
	}

	// 2. Verify with non-existent code
	invNotFoundBody, _ := json.Marshal(map[string]string{"code": "GW-999999"})
	reqNotFound := httptest.NewRequest(http.MethodPost, "/v1/auth/verify-qr", bytes.NewReader(invNotFoundBody))
	reqNotFound.Header.Set("Authorization", "Bearer mock-token-student-gateway")
	recNotFound := httptest.NewRecorder()
	ctxReq.ServeHTTP(recNotFound, reqNotFound)

	if recNotFound.Code != http.StatusNotFound {
		t.Errorf("expected 404 for unknown code, got %d", recNotFound.Code)
	}

	// 3. Verify with expired code
	expInvite, _ := inviteService.CreateInvite("Expired", -5*time.Minute, 1)
	expBody, _ := json.Marshal(map[string]string{"code": expInvite.Code})
	reqExp := httptest.NewRequest(http.MethodPost, "/v1/auth/verify-qr", bytes.NewReader(expBody))
	reqExp.Header.Set("Authorization", "Bearer mock-token-student-gateway")
	recExp := httptest.NewRecorder()
	ctxReq.ServeHTTP(recExp, reqExp)

	if recExp.Code != http.StatusBadRequest {
		t.Errorf("expected 400 for expired code, got %d", recExp.Code)
	}
}

func TestAdminGenerateAndListQR(t *testing.T) {
	_, adminHandler, _, _, _ := setupTestEnv()

	body := map[string]interface{}{
		"label":           "Library Notice Board",
		"durationSeconds": 3600,
		"maxUses":         50,
	}
	b, _ := json.Marshal(body)

	req := httptest.NewRequest(http.MethodPost, "/v1/admin/qr/generate", bytes.NewReader(b))
	rec := httptest.NewRecorder()

	adminHandler.GenerateQR(rec, req)

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created, got %d. body: %s", rec.Code, rec.Body.String())
	}

	var createdInvite invites.Invite
	if err := json.NewDecoder(rec.Body).Decode(&createdInvite); err != nil {
		t.Fatalf("failed to parse generated invite: %v", err)
	}

	if createdInvite.Code == "" || createdInvite.Label != "Library Notice Board" {
		t.Errorf("unexpected invite returned: %+v", createdInvite)
	}

	// Test list
	listReq := httptest.NewRequest(http.MethodGet, "/v1/admin/qr/list", nil)
	listRec := httptest.NewRecorder()
	adminHandler.ListQR(listRec, listReq)

	if listRec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for list, got %d", listRec.Code)
	}

	var list []*invites.Invite
	_ = json.NewDecoder(listRec.Body).Decode(&list)
	if len(list) != 1 || list[0].Code != createdInvite.Code {
		t.Errorf("unexpected list response: %+v", list)
	}
}

func TestLogin_AdminAndStudent(t *testing.T) {
	userService := users.NewService()
	inviteService := invites.NewService()
	verifier := auth.NewMockVerifier()
	authHandler := handlers.NewAuthHandler(userService, inviteService, verifier)

	// 1. Admin login success
	adminPayload := `{"email":"admin@campus.edu","password":"admin123"}`
	req := httptest.NewRequest(http.MethodPost, "/v1/auth/login", strings.NewReader(adminPayload))
	rec := httptest.NewRecorder()
	authHandler.Login(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 for admin login, got %d: %s", rec.Code, rec.Body.String())
	}
	var adminResp handlers.LoginResponse
	_ = json.NewDecoder(rec.Body).Decode(&adminResp)
	if !adminResp.Verified || adminResp.Role != "admin" {
		t.Errorf("expected admin to be verified with admin role, got verified=%v role=%s", adminResp.Verified, adminResp.Role)
	}

	// 2. Student login success
	studentPayload := `{"email":"student@campus.edu","password":"student123"}`
	req = httptest.NewRequest(http.MethodPost, "/v1/auth/login", strings.NewReader(studentPayload))
	rec = httptest.NewRecorder()
	authHandler.Login(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 for student login, got %d: %s", rec.Code, rec.Body.String())
	}
	var studentResp handlers.LoginResponse
	_ = json.NewDecoder(rec.Body).Decode(&studentResp)
	if !studentResp.Verified || studentResp.Role != "student" {
		t.Errorf("expected student to be verified with student role, got verified=%v role=%s", studentResp.Verified, studentResp.Role)
	}

	// 3. Invalid password fails with 401
	badPassPayload := `{"email":"student@campus.edu","password":"wrongpassword"}`
	req = httptest.NewRequest(http.MethodPost, "/v1/auth/login", strings.NewReader(badPassPayload))
	rec = httptest.NewRecorder()
	authHandler.Login(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Errorf("expected 401 for wrong password, got %d", rec.Code)
	}

	// 4. Non-existent email fails with 401
	nonExistentPayload := `{"email":"ghost@campus.edu","password":"password123"}`
	req = httptest.NewRequest(http.MethodPost, "/v1/auth/login", strings.NewReader(nonExistentPayload))
	rec = httptest.NewRecorder()
	authHandler.Login(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Errorf("expected 401 for non-existent user, got %d", rec.Code)
	}
}
