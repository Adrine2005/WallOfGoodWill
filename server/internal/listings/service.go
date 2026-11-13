package listings

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"sort"
	"strings"
	"sync"
	"time"

	"goodwill-server/internal/db"
)

var (
	ErrListingNotFound = errors.New("listing not found")
	ErrInvalidPrice    = errors.New("sale listings must have a price greater than 0")
	ErrUnauthorized    = errors.New("unauthorized: only the seller can modify this listing")
	ErrInvalidInput    = errors.New("title, description, and valid category are required")
)

type ItemType string

const (
	TypeDonate ItemType = "DONATE"
	TypeSell   ItemType = "SELL"
)

type ItemCategory string

const (
	CategoryBooks       ItemCategory = "BOOKS"
	CategoryElectronics ItemCategory = "ELECTRONICS"
	CategoryKitchenware ItemCategory = "KITCHENWARE"
	CategoryCycles      ItemCategory = "CYCLES"
	CategoryFurniture   ItemCategory = "FURNITURE"
	CategoryFashion     ItemCategory = "FASHION"
	CategoryStationery  ItemCategory = "STATIONERY"
	CategoryOther       ItemCategory = "OTHER"
)

type ItemCondition string

const (
	ConditionNew     ItemCondition = "NEW"
	ConditionLikeNew ItemCondition = "LIKE_NEW"
	ConditionGood    ItemCondition = "GOOD"
	ConditionFair    ItemCondition = "FAIR"
)

type ListingStatus string

const (
	StatusActive  ListingStatus = "ACTIVE"
	StatusSold    ListingStatus = "SOLD"
	StatusDonated ListingStatus = "DONATED"
	StatusDeleted ListingStatus = "DELETED"
	StatusHidden  ListingStatus = "HIDDEN"
)

type Listing struct {
	ID          string        `json:"id"`
	SellerID    string        `json:"sellerId"`
	SellerName  string        `json:"sellerName"`
	HostelBlock string        `json:"hostelBlock"`
	RoomNo      string        `json:"roomNo"`
	Title       string        `json:"title"`
	Description string        `json:"description"`
	Category    ItemCategory  `json:"category"`
	Type        ItemType      `json:"type"`
	Price       float64       `json:"price"`
	Condition   ItemCondition `json:"condition"`
	Photos      []string      `json:"photos"`
	Status      ListingStatus `json:"status"`
	CreatedAt   time.Time     `json:"createdAt"`
	UpdatedAt   time.Time     `json:"updatedAt"`
}

type Filter struct {
	Category    ItemCategory `json:"category,omitempty"`
	Type        ItemType     `json:"type,omitempty"`
	HostelBlock string       `json:"hostelBlock,omitempty"`
	Search      string       `json:"search,omitempty"`
}

type Service struct {
	mu       sync.RWMutex
	listings map[string]*Listing
	db       *db.DB
}

func (s *Service) SetDB(database *db.DB) {
	s.db = database
}

func generateID() string {
	b := make([]byte, 6)
	_, _ = rand.Read(b)
	return fmt.Sprintf("lst-%s", hex.EncodeToString(b))
}

func NewService() *Service {
	s := &Service{
		listings: make(map[string]*Listing),
	}
	s.seedDefaultListings()
	return s
}

func (s *Service) seedDefaultListings() {
	now := time.Now().UTC()
	sampleItems := []*Listing{
		{
			ID:          "lst-seed-001",
			SellerID:    "student-sample-1",
			SellerName:  "Rohan Das",
			HostelBlock: "Hostel Block A (Boys)",
			RoomNo:      "302",
			Title:       "Engineering Mathematics (Vol 1 & 2)",
			Description: "Standard semester textbooks in great condition. Donating for free since I have completed the course.",
			Category:    CategoryBooks,
			Type:        TypeDonate,
			Price:       0,
			Condition:   ConditionGood,
			Photos:      []string{"https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"},
			Status:      StatusActive,
			CreatedAt:   now.Add(-2 * time.Hour),
			UpdatedAt:   now.Add(-2 * time.Hour),
		},
		{
			ID:          "lst-seed-002",
			SellerID:    "student-sample-2",
			SellerName:  "Priya Sharma",
			HostelBlock: "Hostel Block C (Girls)",
			RoomNo:      "104",
			Title:       "Electric Kettle 1.5L",
			Description: "Pigeon electric kettle, works perfectly. Great for instant noodles and late night tea/coffee.",
			Category:    CategoryKitchenware,
			Type:        TypeSell,
			Price:       450,
			Condition:   ConditionLikeNew,
			Photos:      []string{"https://images.unsplash.com/photo-1594213114663-ddf3f2a08126?w=500"},
			Status:      StatusActive,
			CreatedAt:   now.Add(-4 * time.Hour),
			UpdatedAt:   now.Add(-4 * time.Hour),
		},
		{
			ID:          "lst-seed-003",
			SellerID:    "student-sample-3",
			SellerName:  "Ankit Verma",
			HostelBlock: "Hostel Block B (Boys)",
			RoomNo:      "215",
			Title:       "Hercules Roadeo Geared Bicycle",
			Description: "Moving out after final semester. Well maintained 21-speed cycle with bell and front basket.",
			Category:    CategoryCycles,
			Type:        TypeSell,
			Price:       2200,
			Condition:   ConditionGood,
			Photos:      []string{"https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=500"},
			Status:      StatusActive,
			CreatedAt:   now.Add(-8 * time.Hour),
			UpdatedAt:   now.Add(-8 * time.Hour),
		},
		{
			ID:          "lst-seed-004",
			SellerID:    "student-sample-4",
			SellerName:  "Kavita Nair",
			HostelBlock: "Hostel Block D (Girls)",
			RoomNo:      "410",
			Title:       "Rechargeable LED Desk Study Lamp",
			Description: "3 brightness settings with warm/cool lighting. Free giveaway for any junior.",
			Category:    CategoryElectronics,
			Type:        TypeDonate,
			Price:       0,
			Condition:   ConditionLikeNew,
			Photos:      []string{"https://images.unsplash.com/photo-1534353436294-0dbd4bdac845?w=500"},
			Status:      StatusActive,
			CreatedAt:   now.Add(-12 * time.Hour),
			UpdatedAt:   now.Add(-12 * time.Hour),
		},
		{
			ID:          "lst-seed-005",
			SellerID:    "student-sample-5",
			SellerName:  "Arun Kumar",
			HostelBlock: "Hostel Block A (Boys)",
			RoomNo:      "112",
			Title:       "Engineering Graphics Drafter & Tube",
			Description: "Mini drafter with scale, clamps, and waterproof drawing sheet carrier tube. Free donation.",
			Category:    CategoryStationery,
			Type:        TypeDonate,
			Price:       0,
			Condition:   ConditionGood,
			Photos:      []string{"https://images.unsplash.com/photo-1585776245991-cf89dd7fc73a?w=500"},
			Status:      StatusActive,
			CreatedAt:   now.Add(-14 * time.Hour),
			UpdatedAt:   now.Add(-14 * time.Hour),
		},
		{
			ID:          "lst-seed-006",
			SellerID:    "student-sample-6",
			SellerName:  "Sneha Patel",
			HostelBlock: "Hostel Block C (Girls)",
			RoomNo:      "305",
			Title:       "Bedside Wooden Study Table",
			Description: "Foldable wooden laptop and study desk with cup holder. Ideal for small hostel rooms.",
			Category:    CategoryFurniture,
			Type:        TypeSell,
			Price:       550,
			Condition:   ConditionLikeNew,
			Photos:      []string{"https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=500"},
			Status:      StatusActive,
			CreatedAt:   now.Add(-18 * time.Hour),
			UpdatedAt:   now.Add(-18 * time.Hour),
		},
	}

	for _, item := range sampleItems {
		s.listings[item.ID] = item
	}
}

func (s *Service) CreateListing(sellerID, sellerName, hostelBlock, roomNo, title, description string,
	category ItemCategory, itemType ItemType, price float64, condition ItemCondition, photos []string) (*Listing, error) {

	s.mu.Lock()
	defer s.mu.Unlock()

	cleanTitle := strings.TrimSpace(title)
	cleanDesc := strings.TrimSpace(description)
	if cleanTitle == "" || cleanDesc == "" {
		return nil, ErrInvalidInput
	}

	if itemType == TypeDonate {
		price = 0
	} else if itemType == TypeSell {
		if price <= 0 {
			return nil, ErrInvalidPrice
		}
	} else {
		return nil, errors.New("invalid item type")
	}

	if condition == "" {
		condition = ConditionGood
	}
	if category == "" {
		category = CategoryOther
	}

	now := time.Now().UTC()
	listing := &Listing{
		ID:          generateID(),
		SellerID:    strings.TrimSpace(sellerID),
		SellerName:  strings.TrimSpace(sellerName),
		HostelBlock: strings.TrimSpace(hostelBlock),
		RoomNo:      strings.TrimSpace(roomNo),
		Title:       cleanTitle,
		Description: cleanDesc,
		Category:    category,
		Type:        itemType,
		Price:       price,
		Condition:   condition,
		Photos:      photos,
		Status:      StatusActive,
		CreatedAt:   now,
		UpdatedAt:   now,
	}

	s.listings[listing.ID] = listing

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_, _ = s.db.Pool.Exec(ctx, `
			INSERT INTO listings (id, seller_id, seller_name, hostel_block, room_no, title, description, category, type, price, condition, status, created_at, updated_at)
			VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
			ON CONFLICT (id) DO UPDATE SET
				title = EXCLUDED.title,
				description = EXCLUDED.description,
				category = EXCLUDED.category,
				type = EXCLUDED.type,
				price = EXCLUDED.price,
				condition = EXCLUDED.condition,
				status = EXCLUDED.status,
				updated_at = EXCLUDED.updated_at
		`, listing.ID, listing.SellerID, listing.SellerName, listing.HostelBlock, listing.RoomNo, listing.Title, listing.Description, listing.Category, listing.Type, listing.Price, listing.Condition, listing.Status, listing.CreatedAt, listing.UpdatedAt)

		for i, photo := range listing.Photos {
			_, _ = s.db.Pool.Exec(ctx, `
				INSERT INTO listing_photos (listing_id, photo_url, display_order)
				VALUES ($1, $2, $3)
			`, listing.ID, photo, i)
		}
	}

	return listing, nil
}

func (s *Service) GetListing(id string) (*Listing, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	item, exists := s.listings[strings.TrimSpace(id)]
	if !exists || item.Status == StatusDeleted {
		return nil, ErrListingNotFound
	}
	return item, nil
}

func (s *Service) ListListings(filter Filter) []*Listing {
	s.mu.RLock()
	defer s.mu.RUnlock()

	var result []*Listing
	cleanSearch := strings.ToLower(strings.TrimSpace(filter.Search))

	for _, item := range s.listings {
		if item.Status != StatusActive {
			continue
		}

		if filter.Category != "" && item.Category != filter.Category {
			continue
		}
		if filter.Type != "" && item.Type != filter.Type {
			continue
		}
		if filter.HostelBlock != "" && !strings.EqualFold(item.HostelBlock, filter.HostelBlock) {
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

	// Sort newest first
	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.After(result[j].CreatedAt)
	})

	return result
}

func (s *Service) GetMyListings(sellerID string) []*Listing {
	s.mu.RLock()
	defer s.mu.RUnlock()

	var result []*Listing
	cleanSeller := strings.TrimSpace(sellerID)

	for _, item := range s.listings {
		if item.SellerID == cleanSeller && item.Status != StatusDeleted {
			result = append(result, item)
		}
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.After(result[j].CreatedAt)
	})

	return result
}

func (s *Service) DeleteListing(id string, sellerID string) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	item, exists := s.listings[strings.TrimSpace(id)]
	if !exists || item.Status == StatusDeleted {
		return ErrListingNotFound
	}

	if item.SellerID != strings.TrimSpace(sellerID) {
		return ErrUnauthorized
	}

	item.Status = StatusDeleted
	item.UpdatedAt = time.Now().UTC()

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_, _ = s.db.Pool.Exec(ctx, `UPDATE listings SET status = 'DELETED', updated_at = $1 WHERE id = $2`, item.UpdatedAt, item.ID)
	}

	return nil
}

func (s *Service) UpdateStatus(id string, sellerID string, status ListingStatus) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	item, exists := s.listings[strings.TrimSpace(id)]
	if !exists || item.Status == StatusDeleted {
		return ErrListingNotFound
	}

	if item.SellerID != strings.TrimSpace(sellerID) {
		return ErrUnauthorized
	}

	item.Status = status
	item.UpdatedAt = time.Now().UTC()

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_, _ = s.db.Pool.Exec(ctx, `UPDATE listings SET status = $1, updated_at = $2 WHERE id = $3`, string(item.Status), item.UpdatedAt, item.ID)
	}

	return nil
}

func (s *Service) SetSystemStatus(id string, status ListingStatus) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	item, exists := s.listings[strings.TrimSpace(id)]
	if !exists || item.Status == StatusDeleted {
		return ErrListingNotFound
	}

	item.Status = status
	item.UpdatedAt = time.Now().UTC()

	if s.db != nil && s.db.Pool != nil {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_, _ = s.db.Pool.Exec(ctx, `UPDATE listings SET status = $1, updated_at = $2 WHERE id = $3`, string(item.Status), item.UpdatedAt, item.ID)
	}

	return nil
}

type UpdateListingParams struct {
	Title       string
	Description string
	Category    ItemCategory
	Price       float64
	Condition   ItemCondition
	Photos      []string
}

func (s *Service) UpdateListing(id string, sellerID string, params UpdateListingParams) (*Listing, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	item, exists := s.listings[strings.TrimSpace(id)]
	if !exists || item.Status == StatusDeleted {
		return nil, ErrListingNotFound
	}

	if item.SellerID != strings.TrimSpace(sellerID) {
		return nil, ErrUnauthorized
	}

	if strings.TrimSpace(params.Title) != "" {
		item.Title = strings.TrimSpace(params.Title)
	}
	if strings.TrimSpace(params.Description) != "" {
		item.Description = strings.TrimSpace(params.Description)
	}
	if params.Category != "" {
		item.Category = params.Category
	}
	if params.Condition != "" {
		item.Condition = params.Condition
	}
	if item.Type == TypeSell && params.Price > 0 {
		item.Price = params.Price
	}
	if params.Photos != nil {
		item.Photos = params.Photos
	}

	item.UpdatedAt = time.Now().UTC()
	return item, nil
}

