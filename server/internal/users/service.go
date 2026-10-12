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
	Password        string    `json:"-"`
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
	s := &Service{
		users: make(map[string]*User),
	}
	s.seedDefaultUsers()
	return s
}

func (s *Service) seedDefaultUsers() {
	now := time.Now().UTC()

	// 1. Management / Admin Account (Pre-verified)
	admin := &User{
		UID:         "admin-101",
		Name:        "Campus Management (Admin)",
		Email:       "admin@campus.edu",
		Password:    "admin123",
		HostelBlock: "Admin Office",
		RoomNo:      "A-01",
		Phone:       "+91 9876543210",
		Role:        "admin",
		Verified:    true,
		CreatedAt:   now,
		UpdatedAt:   now,
	}
	s.users[admin.UID] = admin

	// 2. Verified Student (Pre-verified)
	student := &User{
		UID:         "student-101",
		Name:        "Adrine Solomon",
		Email:       "student@campus.edu",
		Password:    "student123",
		HostelBlock: "Hostel Block A (Boys)",
		RoomNo:      "302",
		Phone:       "+91 9123456780",
		Role:        "student",
		Verified:    true,
		CreatedAt:   now,
		UpdatedAt:   now,
	}
	s.users[student.UID] = student

	// 3. Junior Student (Unverified - for testing QR verification)
	junior := &User{
		UID:         "junior-102",
		Name:        "Junior Student",
		Email:       "junior@campus.edu",
		Password:    "junior123",
		HostelBlock: "Hostel Block B (Boys)",
		RoomNo:      "105",
		Phone:       "+91 9988776655",
		Role:        "student",
		Verified:    false,
		CreatedAt:   now,
		UpdatedAt:   now,
	}
	s.users[junior.UID] = junior
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

func (s *Service) GetUserByEmail(email string) (*User, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanEmail := strings.ToLower(strings.TrimSpace(email))
	for _, u := range s.users {
		if strings.ToLower(u.Email) == cleanEmail {
			return u, nil
		}
	}
	return nil, ErrUserNotFound
}
