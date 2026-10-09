package requests

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
	ErrRequestNotFound     = errors.New("donation request not found")
	ErrListingNotDonation  = errors.New("only donation items can be requested")
	ErrListingNotActive    = errors.New("listing is no longer active")
	ErrSelfRequest         = errors.New("cannot request your own donation listing")
	ErrUnauthorized        = errors.New("unauthorized: only the donor can manage requests")
	ErrAlreadyResolved     = errors.New("request has already been resolved")
	ErrEmptyMessage        = errors.New("request message cannot be empty")
)

type RequestStatus string

const (
	StatusPending   RequestStatus = "PENDING"
	StatusAccepted  RequestStatus = "ACCEPTED"
	StatusDeclined  RequestStatus = "DECLINED"
	StatusCancelled RequestStatus = "CANCELLED"
)

type DonationRequest struct {
	ID              string        `json:"id"`
	ListingID       string        `json:"listingId"`
	ListingTitle    string        `json:"listingTitle"`
	DonorID         string        `json:"donorId"`
	DonorName       string        `json:"donorName"`
	RequesterID     string        `json:"requesterId"`
	RequesterName   string        `json:"requesterName"`
	RequesterHostel string        `json:"requesterHostel"`
	RequesterRoom   string        `json:"requesterRoom"`
	Message         string        `json:"message"`
	Status          RequestStatus `json:"status"`
	CreatedAt       time.Time     `json:"createdAt"`
	UpdatedAt       time.Time     `json:"updatedAt"`
}

type Service struct {
	mu              sync.RWMutex
	requests        map[string]*DonationRequest
	listingsService *listings.Service
	usersService    *users.Service
}

func NewService(listingsService *listings.Service, usersService *users.Service) *Service {
	return &Service{
		requests:        make(map[string]*DonationRequest),
		listingsService: listingsService,
		usersService:    usersService,
	}
}

func generateID() string {
	b := make([]byte, 4)
	_, _ = rand.Read(b)
	return fmt.Sprintf("req-%s", hex.EncodeToString(b))
}

func (s *Service) CreateRequest(listingID string, requesterID string, message string) (*DonationRequest, error) {
	cleanMessage := strings.TrimSpace(message)
	if cleanMessage == "" {
		return nil, ErrEmptyMessage
	}

	listing, err := s.listingsService.GetListing(listingID)
	if err != nil {
		return nil, err
	}

	if listing.Type != listings.TypeDonate {
		return nil, ErrListingNotDonation
	}

	if listing.Status != listings.StatusActive {
		return nil, ErrListingNotActive
	}

	if listing.SellerID == strings.TrimSpace(requesterID) {
		return nil, ErrSelfRequest
	}

	requester, err := s.usersService.GetUser(requesterID)
	reqName := "Campus Student"
	reqHostel := "Campus Hostel"
	reqRoom := ""
	if err == nil && requester != nil {
		if requester.Name != "" {
			reqName = requester.Name
		}
		if requester.HostelBlock != "" {
			reqHostel = requester.HostelBlock
		}
		reqRoom = requester.RoomNo
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	now := time.Now().UTC()
	req := &DonationRequest{
		ID:              generateID(),
		ListingID:       listing.ID,
		ListingTitle:    listing.Title,
		DonorID:         listing.SellerID,
		DonorName:       listing.SellerName,
		RequesterID:     requesterID,
		RequesterName:   reqName,
		RequesterHostel: reqHostel,
		RequesterRoom:   reqRoom,
		Message:         cleanMessage,
		Status:          StatusPending,
		CreatedAt:       now,
		UpdatedAt:       now,
	}

	s.requests[req.ID] = req
	return req, nil
}

func (s *Service) GetListingRequests(listingID string, donorID string) ([]*DonationRequest, error) {
	listing, err := s.listingsService.GetListing(listingID)
	if err != nil {
		return nil, err
	}

	if listing.SellerID != strings.TrimSpace(donorID) {
		return nil, ErrUnauthorized
	}

	s.mu.RLock()
	defer s.mu.RUnlock()

	var result []*DonationRequest
	for _, req := range s.requests {
		if req.ListingID == listingID {
			result = append(result, req)
		}
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.After(result[j].CreatedAt)
	})

	return result, nil
}

func (s *Service) GetIncomingRequests(donorID string) []*DonationRequest {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanDonor := strings.TrimSpace(donorID)
	var result []*DonationRequest
	for _, req := range s.requests {
		if req.DonorID == cleanDonor {
			result = append(result, req)
		}
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.After(result[j].CreatedAt)
	})

	return result
}

func (s *Service) AcceptRequest(requestID string, donorID string) (*DonationRequest, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	target, exists := s.requests[strings.TrimSpace(requestID)]
	if !exists {
		return nil, ErrRequestNotFound
	}

	if target.DonorID != strings.TrimSpace(donorID) {
		return nil, ErrUnauthorized
	}

	if target.Status != StatusPending {
		return nil, ErrAlreadyResolved
	}

	now := time.Now().UTC()
	target.Status = StatusAccepted
	target.UpdatedAt = now

	// Auto-decline all other pending requests for the same listing
	for _, req := range s.requests {
		if req.ListingID == target.ListingID && req.ID != target.ID && req.Status == StatusPending {
			req.Status = StatusDeclined
			req.UpdatedAt = now
		}
	}

	// Update the listing status to DONATED
	_ = s.listingsService.UpdateStatus(target.ListingID, donorID, listings.StatusDonated)

	return target, nil
}

func (s *Service) DeclineRequest(requestID string, donorID string) (*DonationRequest, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	target, exists := s.requests[strings.TrimSpace(requestID)]
	if !exists {
		return nil, ErrRequestNotFound
	}

	if target.DonorID != strings.TrimSpace(donorID) {
		return nil, ErrUnauthorized
	}

	if target.Status != StatusPending {
		return nil, ErrAlreadyResolved
	}

	target.Status = StatusDeclined
	target.UpdatedAt = time.Now().UTC()
	return target, nil
}
