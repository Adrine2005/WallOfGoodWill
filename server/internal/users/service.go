package users

import (
	"context"
	"errors"
	"strings"
	"sync"
	"time"

	"goodwill-server/internal/db"
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
	db    *db.DB
}

func NewService() *Service {
	s := &Service{
		users: make(map[string]*User),
	}
	s.seedDefaultUsers()
	return s
}

func (s *Service) SetDB(database *db.DB) {
	s.db = database
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

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_, _ = s.db.Pool.Exec(ctx, `
			INSERT INTO users (id, name, email, password_hash, role, hostel_block, room_no, phone, verified, verified_via_code, created_at)
			VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
			ON CONFLICT (id) DO UPDATE SET
				name = EXCLUDED.name,
				email = EXCLUDED.email,
				password_hash = EXCLUDED.password_hash,
				role = EXCLUDED.role,
				hostel_block = EXCLUDED.hostel_block,
				room_no = EXCLUDED.room_no,
				phone = EXCLUDED.phone,
				verified = EXCLUDED.verified,
				verified_via_code = EXCLUDED.verified_via_code
		`, u.UID, u.Name, u.Email, u.Password, u.Role, u.HostelBlock, u.RoomNo, u.Phone, u.Verified, u.VerifiedViaCode, u.CreatedAt)
	}
}

func (s *Service) GetUser(uid string) (*User, error) {
	cleanUID := strings.TrimSpace(uid)
	s.mu.RLock()
	user, exists := s.users[cleanUID]
	s.mu.RUnlock()
	if exists {
		return user, nil
	}

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		var u User
		err := s.db.Pool.QueryRow(ctx, `
			SELECT id, name, email, password_hash, role, hostel_block, room_no, phone, verified, verified_via_code, created_at
			FROM users WHERE id = $1
		`, cleanUID).Scan(&u.UID, &u.Name, &u.Email, &u.Password, &u.Role, &u.HostelBlock, &u.RoomNo, &u.Phone, &u.Verified, &u.VerifiedViaCode, &u.CreatedAt)
		if err == nil {
			s.mu.Lock()
			s.users[u.UID] = &u
			s.mu.Unlock()
			return &u, nil
		}
	}

	return nil, ErrUserNotFound
}

func (s *Service) GetUserByEmail(email string) (*User, error) {
	cleanEmail := strings.ToLower(strings.TrimSpace(email))
	s.mu.RLock()
	for _, u := range s.users {
		if strings.ToLower(u.Email) == cleanEmail {
			s.mu.RUnlock()
			return u, nil
		}
	}
	s.mu.RUnlock()

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		var u User
		err := s.db.Pool.QueryRow(ctx, `
			SELECT id, name, email, password_hash, role, hostel_block, room_no, phone, verified, verified_via_code, created_at
			FROM users WHERE LOWER(email) = $1
		`, cleanEmail).Scan(&u.UID, &u.Name, &u.Email, &u.Password, &u.Role, &u.HostelBlock, &u.RoomNo, &u.Phone, &u.Verified, &u.VerifiedViaCode, &u.CreatedAt)
		if err == nil {
			s.mu.Lock()
			s.users[u.UID] = &u
			s.mu.Unlock()
			return &u, nil
		}
	}

	return nil, ErrUserNotFound
}
