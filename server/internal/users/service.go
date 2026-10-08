package users

import (
	"errors"
	"strings"
	"sync"
	"time"
)

var (
	ErrUserNotFound = errors.New("user not found")
)

type User struct {
	UID             string    `json:"uid"`
	Name            string    `json:"name"`
	Email           string    `json:"email"`
	HostelBlock     string    `json:"hostelBlock"`
	RoomNo          string    `json:"roomNo"`
	Phone           string    `json:"phone,omitempty"`
	Role            string    `json:"role"`
	Verified        bool      `json:"verified"`
	VerifiedViaCode string    `json:"verifiedViaCode,omitempty"`
	CreatedAt       time.Time `json:"createdAt"`
	UpdatedAt       time.Time `json:"updatedAt"`
}

type Service struct {
	mu    sync.RWMutex
	users map[string]*User
}

func NewService() *Service {
	return &Service{
		users: make(map[string]*User),
	}
}

func (s *Service) SaveUser(u *User) {
	s.mu.Lock()
	defer s.mu.Unlock()

	u.UpdatedAt = time.Now().UTC()
	if u.CreatedAt.IsZero() {
		u.CreatedAt = u.UpdatedAt
	}
	s.users[u.UID] = u
}

func (s *Service) GetUser(uid string) (*User, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	user, exists := s.users[strings.TrimSpace(uid)]
	if !exists {
		return nil, ErrUserNotFound
	}
	return user, nil
}
