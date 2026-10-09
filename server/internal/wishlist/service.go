package wishlist

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"sort"
	"strings"
	"sync"
	"time"

	"goodwill-server/internal/listings"
	"goodwill-server/internal/users"
)

var (
	ErrWishlistNotFound = errors.New("wishlist item not found")
	ErrInvalidInput     = errors.New("title and category are required")
	ErrUnauthorized     = errors.New("unauthorized: only the requester can modify this wishlist item")
)

type WishlistStatus string

const (
	StatusActive    WishlistStatus = "ACTIVE"
	StatusFulfilled WishlistStatus = "FULFILLED"
	StatusDeleted   WishlistStatus = "DELETED"
)

type WishlistItem struct {
	ID                  string                `json:"id"`
	RequesterID         string                `json:"requesterId"`
	RequesterName       string                `json:"requesterName"`
	RequesterHostel     string                `json:"requesterHostel"`
	RequesterRoom       string                `json:"requesterRoom"`
	Title               string                `json:"title"`
	Description         string                `json:"description"`
	Category            listings.ItemCategory `json:"category"`
	IsDonationPreferred bool                  `json:"isDonationPreferred"`
	MaxBudget           float64               `json:"maxBudget"`
	Status              WishlistStatus        `json:"status"`
	CreatedAt           time.Time             `json:"createdAt"`
	UpdatedAt           time.Time             `json:"updatedAt"`
}

type Service struct {
	mu           sync.RWMutex
	items        map[string]*WishlistItem
	usersService *users.Service
}

func NewService(u *users.Service) *Service {
	s := &Service{
		items:        make(map[string]*WishlistItem),
		usersService: u,
	}
	s.seedWishlist()
	return s
}

func generateID() string {
	b := make([]byte, 4)
	_, _ = rand.Read(b)
	return fmt.Sprintf("wish-%s", hex.EncodeToString(b))
}

func (s *Service) seedWishlist() {
	now := time.Now().UTC()
	seeds := []*WishlistItem{
		{
			ID:                  "wish-seed-1",
			RequesterID:         "student-priya",
			RequesterName:       "Priya Sharma",
			RequesterHostel:     "Hostel B (Girls)",
			RequesterRoom:       "204",
			Title:               "Engineering Graphics Mini Drafter",
			Description:         "Looking for a mini drafter in working condition for Semester 1 graphics lab.",
			Category:            listings.CategoryStationery,
			IsDonationPreferred: true,
			MaxBudget:           0,
			Status:              StatusActive,
			CreatedAt:           now.Add(-4 * time.Hour),
			UpdatedAt:           now.Add(-4 * time.Hour),
		},
		{
			ID:                  "wish-seed-2",
			RequesterID:         "student-arjun",
			RequesterName:       "Arjun Patel",
			RequesterHostel:     "Hostel A (Boys)",
			RequesterRoom:       "112",
			Title:               "Electric Kettle (1.5L or 1.8L)",
			Description:         "Needed for late night exam prep in winter semester. Ready to buy.",
			Category:            listings.CategoryKitchenware,
			IsDonationPreferred: false,
			MaxBudget:           350,
			Status:              StatusActive,
			CreatedAt:           now.Add(-2 * time.Hour),
			UpdatedAt:           now.Add(-2 * time.Hour),
		},
		{
			ID:                  "wish-seed-3",
			RequesterID:         "student-sneha",
			RequesterName:       "Sneha Kulkarni",
			RequesterHostel:     "Hostel C (Girls)",
			RequesterRoom:       "308",
			Title:               "Data Structures & Algorithms in Java (Robert Lafore)",
			Description:         "2nd year CS course reference book. Can exchange or buy.",
			Category:            listings.CategoryBooks,
			IsDonationPreferred: false,
			MaxBudget:           250,
			Status:              StatusActive,
			CreatedAt:           now.Add(-1 * time.Hour),
			UpdatedAt:           now.Add(-1 * time.Hour),
		},
	}

	for _, item := range seeds {
		s.items[item.ID] = item
	}
}

func (s *Service) CreateWishlistItem(requesterID, requesterName, requesterHostel, requesterRoom, title, description string, category listings.ItemCategory, isDonationPreferred bool, maxBudget float64) (*WishlistItem, error) {
	cleanTitle := strings.TrimSpace(title)
	if cleanTitle == "" || strings.TrimSpace(string(category)) == "" {
		return nil, ErrInvalidInput
	}

	cleanReqID := strings.TrimSpace(requesterID)
	name := strings.TrimSpace(requesterName)
	hostel := strings.TrimSpace(requesterHostel)
	room := strings.TrimSpace(requesterRoom)

	if s.usersService != nil {
		u, _ := s.usersService.GetUser(cleanReqID)
		if u != nil {
			if name == "" {
				name = u.Name
			}
			if hostel == "" {
				hostel = u.HostelBlock
			}
			if room == "" {
				room = u.RoomNo
			}
		}
	}

	now := time.Now().UTC()
	item := &WishlistItem{
		ID:                  generateID(),
		RequesterID:         cleanReqID,
		RequesterName:       name,
		RequesterHostel:     hostel,
		RequesterRoom:       room,
		Title:               cleanTitle,
		Description:         strings.TrimSpace(description),
		Category:            category,
		IsDonationPreferred: isDonationPreferred,
		MaxBudget:           maxBudget,
		Status:              StatusActive,
		CreatedAt:           now,
		UpdatedAt:           now,
	}

	s.mu.Lock()
	defer s.mu.Unlock()
	s.items[item.ID] = item
	return item, nil
}

func (s *Service) ListWishlistItems(category, search string) []*WishlistItem {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanCat := strings.ToUpper(strings.TrimSpace(category))
	cleanSearch := strings.ToLower(strings.TrimSpace(search))

	var result []*WishlistItem
	for _, item := range s.items {
		if item.Status != StatusActive {
			continue
		}

		if cleanCat != "" && strings.ToUpper(string(item.Category)) != cleanCat {
			continue
		}

		if cleanSearch != "" {
			titleMatch := strings.Contains(strings.ToLower(item.Title), cleanSearch)
			descMatch := strings.Contains(strings.ToLower(item.Description), cleanSearch)
			if !titleMatch && !descMatch {
				continue
			}
		}

		result = append(result, item)
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.After(result[j].CreatedAt)
	})

	return result
}

func (s *Service) GetMyWishlistItems(requesterID string) []*WishlistItem {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanID := strings.TrimSpace(requesterID)
	var result []*WishlistItem
	for _, item := range s.items {
		if item.RequesterID == cleanID && item.Status != StatusDeleted {
			result = append(result, item)
		}
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.After(result[j].CreatedAt)
	})

	return result
}

func (s *Service) GetWishlistItem(id string) (*WishlistItem, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanID := strings.TrimSpace(id)
	item, exists := s.items[cleanID]
	if !exists || item.Status == StatusDeleted {
		return nil, ErrWishlistNotFound
	}
	return item, nil
}

func (s *Service) DeleteWishlistItem(id, requesterID string) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleanID := strings.TrimSpace(id)
	cleanReqID := strings.TrimSpace(requesterID)

	item, exists := s.items[cleanID]
	if !exists || item.Status == StatusDeleted {
		return ErrWishlistNotFound
	}

	if item.RequesterID != cleanReqID {
		return ErrUnauthorized
	}

	item.Status = StatusDeleted
	item.UpdatedAt = time.Now().UTC()
	return nil
}
