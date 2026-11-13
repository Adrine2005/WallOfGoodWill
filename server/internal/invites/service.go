package invites

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"

	"goodwill-server/internal/db"
)

var (
	ErrInviteNotFound  = errors.New("invite code not found")
	ErrInviteExpired   = errors.New("invite code has expired")
	ErrInviteExhausted = errors.New("invite code usage limit reached")
	ErrInviteInactive  = errors.New("invite code is inactive")
)

type Invite struct {
	Code      string    `json:"code"`
	Label     string    `json:"label"`
	CreatedAt time.Time `json:"createdAt"`
	ExpiresAt time.Time `json:"expiresAt"`
	MaxUses   int       `json:"maxUses"` // 0 means unlimited within time window
	UsedCount int       `json:"usedCount"`
	IsActive  bool      `json:"isActive"`
}

type Service struct {
	mu      sync.RWMutex
	invites map[string]*Invite
	db      *db.DB
}

func NewService() *Service {
	return &Service{
		invites: make(map[string]*Invite),
	}
}

func (s *Service) SetDB(database *db.DB) {
	s.db = database
}

func (s *Service) SeedDefaultInvites() {
	now := time.Now().UTC()
	expiry := now.Add(365 * 24 * time.Hour) // 1 year

	s.invites["GW-NOTICEBOARD-A"] = &Invite{
		Code:      "GW-NOTICEBOARD-A",
		Label:     "Hostel Block A Main Notice Board",
		CreatedAt: now,
		ExpiresAt: expiry,
		MaxUses:   1000,
		UsedCount: 0,
		IsActive:  true,
	}

	s.invites["GW-31D63A7C"] = &Invite{
		Code:      "GW-31D63A7C",
		Label:     "Campus Main Notice Board Poster",
		CreatedAt: now,
		ExpiresAt: expiry,
		MaxUses:   1000,
		UsedCount: 0,
		IsActive:  true,
	}
}

func generateCode() (string, error) {
	bytes := make([]byte, 4)
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}
	return fmt.Sprintf("GW-%s", strings.ToUpper(hex.EncodeToString(bytes))), nil
}

func (s *Service) CreateInvite(label string, duration time.Duration, maxUses int) (*Invite, error) {
	code, err := generateCode()
	if err != nil {
		return nil, err
	}

	now := time.Now().UTC()
	invite := &Invite{
		Code:      code,
		Label:     label,
		CreatedAt: now,
		ExpiresAt: now.Add(duration),
		MaxUses:   maxUses,
		UsedCount: 0,
		IsActive:  true,
	}

	s.mu.Lock()
	s.invites[code] = invite
	s.mu.Unlock()

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_, _ = s.db.Pool.Exec(ctx, `
			INSERT INTO campus_invites (code, label, expires_at, max_uses, used_count, is_active, created_at)
			VALUES ($1, $2, $3, $4, $5, $6, $7)
			ON CONFLICT (code) DO NOTHING
		`, invite.Code, invite.Label, invite.ExpiresAt, invite.MaxUses, invite.UsedCount, invite.IsActive, invite.CreatedAt)
	}

	return invite, nil
}

func (s *Service) GetInvite(code string) (*Invite, error) {
	cleanCode := strings.TrimSpace(code)
	s.mu.RLock()
	invite, exists := s.invites[cleanCode]
	s.mu.RUnlock()
	if exists {
		return invite, nil
	}

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		var inv Invite
		err := s.db.Pool.QueryRow(ctx, `
			SELECT code, label, expires_at, max_uses, used_count, is_active, created_at
			FROM campus_invites WHERE code = $1
		`, cleanCode).Scan(&inv.Code, &inv.Label, &inv.ExpiresAt, &inv.MaxUses, &inv.UsedCount, &inv.IsActive, &inv.CreatedAt)
		if err == nil {
			s.mu.Lock()
			s.invites[inv.Code] = &inv
			s.mu.Unlock()
			return &inv, nil
		}
	}

	return nil, ErrInviteNotFound
}

func (s *Service) ValidateAndUseInvite(code string) (*Invite, error) {
	invite, err := s.GetInvite(code)
	if err != nil {
		return nil, err
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	if !invite.IsActive {
		return nil, ErrInviteInactive
	}

	if time.Now().UTC().After(invite.ExpiresAt) {
		return nil, ErrInviteExpired
	}

	if invite.MaxUses > 0 && invite.UsedCount >= invite.MaxUses {
		return nil, ErrInviteExhausted
	}

	invite.UsedCount++

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_, _ = s.db.Pool.Exec(ctx, `
			UPDATE campus_invites SET used_count = used_count + 1 WHERE code = $1
		`, invite.Code)
	}

	return invite, nil
}

func (s *Service) ListInvites() []*Invite {
	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		rows, err := s.db.Pool.Query(ctx, `
			SELECT code, label, expires_at, max_uses, used_count, is_active, created_at
			FROM campus_invites ORDER BY created_at DESC
		`)
		if err == nil {
			defer rows.Close()
			var list []*Invite
			for rows.Next() {
				var inv Invite
				if err := rows.Scan(&inv.Code, &inv.Label, &inv.ExpiresAt, &inv.MaxUses, &inv.UsedCount, &inv.IsActive, &inv.CreatedAt); err == nil {
					list = append(list, &inv)
					s.mu.Lock()
					s.invites[inv.Code] = &inv
					s.mu.Unlock()
				}
			}
			if len(list) > 0 {
				return list
			}
		}
	}

	s.mu.RLock()
	defer s.mu.RUnlock()

	list := make([]*Invite, 0, len(s.invites))
	for _, inv := range s.invites {
		list = append(list, inv)
	}
	return list
}
