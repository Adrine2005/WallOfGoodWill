package invites

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"
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
}

func NewService() *Service {
	return &Service{
		invites: make(map[string]*Invite),
	}
}

// GenerateCode creates a random unique campus code like "GW-A9B4C2"
func generateCode() (string, error) {
	bytes := make([]byte, 4)
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}
	return fmt.Sprintf("GW-%s", strings.ToUpper(hex.EncodeToString(bytes))), nil
}

func (s *Service) CreateInvite(label string, duration time.Duration, maxUses int) (*Invite, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

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

	s.invites[code] = invite
	return invite, nil
}

func (s *Service) GetInvite(code string) (*Invite, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	invite, exists := s.invites[strings.TrimSpace(code)]
	if !exists {
		return nil, ErrInviteNotFound
	}
	return invite, nil
}

func (s *Service) ValidateAndUseInvite(code string) (*Invite, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleanCode := strings.TrimSpace(code)
	invite, exists := s.invites[cleanCode]
	if !exists {
		return nil, ErrInviteNotFound
	}

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
	return invite, nil
}

func (s *Service) ListInvites() []*Invite {
	s.mu.RLock()
	defer s.mu.RUnlock()

	list := make([]*Invite, 0, len(s.invites))
	for _, inv := range s.invites {
		list = append(list, inv)
	}
	return list
}
