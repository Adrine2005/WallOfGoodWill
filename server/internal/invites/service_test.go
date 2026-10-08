package invites_test

import (
	"testing"
	"time"

	"goodwill-server/internal/invites"
)

func TestInviteLifecycle(t *testing.T) {
	svc := invites.NewService()

	// 1. Create active invite
	inv, err := svc.CreateInvite("Hostel A Notice Board", 1*time.Hour, 2)
	if err != nil {
		t.Fatalf("failed to create invite: %v", err)
	}

	if inv.Code == "" || inv.UsedCount != 0 || !inv.IsActive {
		t.Fatalf("unexpected invite state: %+v", inv)
	}

	// 2. Validate and use (first time)
	used1, err := svc.ValidateAndUseInvite(inv.Code)
	if err != nil {
		t.Fatalf("expected valid invite, got error: %v", err)
	}
	if used1.UsedCount != 1 {
		t.Errorf("expected usedCount 1, got %d", used1.UsedCount)
	}

	// 3. Validate and use (second time)
	used2, err := svc.ValidateAndUseInvite(inv.Code)
	if err != nil {
		t.Fatalf("expected valid invite second time, got error: %v", err)
	}
	if used2.UsedCount != 2 {
		t.Errorf("expected usedCount 2, got %d", used2.UsedCount)
	}

	// 4. Third time should fail due to maxUses limit
	_, err = svc.ValidateAndUseInvite(inv.Code)
	if err != invites.ErrInviteExhausted {
		t.Errorf("expected ErrInviteExhausted, got %v", err)
	}
}

func TestInviteExpiration(t *testing.T) {
	svc := invites.NewService()

	// Create invite with negative duration (already expired)
	inv, err := svc.CreateInvite("Quick Expiry", -1*time.Minute, 10)
	if err != nil {
		t.Fatalf("failed to create invite: %v", err)
	}

	_, err = svc.ValidateAndUseInvite(inv.Code)
	if err != invites.ErrInviteExpired {
		t.Errorf("expected ErrInviteExpired, got %v", err)
	}
}

func TestInviteNotFound(t *testing.T) {
	svc := invites.NewService()

	_, err := svc.ValidateAndUseInvite("NON-EXISTENT-CODE")
	if err != invites.ErrInviteNotFound {
		t.Errorf("expected ErrInviteNotFound, got %v", err)
	}
}
