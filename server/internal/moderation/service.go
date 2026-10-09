package moderation

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"

	"goodwill-server/internal/listings"
)

var (
	ErrListingNotFound = errors.New("listing not found")
	ErrEmptyReason     = errors.New("report reason cannot be empty")
	ErrInvalidReason   = errors.New("invalid report reason")
	ErrSelfReporting   = errors.New("cannot report your own listing")
)

type ReportReason string

const (
	ReasonProhibitedItem       ReportReason = "PROHIBITED_ITEM"
	ReasonInappropriateContent ReportReason = "INAPPROPRIATE_CONTENT"
	ReasonSpamOrScam           ReportReason = "SPAM_OR_SCAM"
	ReasonOther                ReportReason = "OTHER"
)

func IsValidReason(r ReportReason) bool {
	switch r {
	case ReasonProhibitedItem, ReasonInappropriateContent, ReasonSpamOrScam, ReasonOther:
		return true
	default:
		return false
	}
}

type Report struct {
	ID         string       `json:"id"`
	ListingID  string       `json:"listingId"`
	ReporterID string       `json:"reporterId"`
	Reason     ReportReason `json:"reason"`
	Comment    string       `json:"comment"`
	CreatedAt  time.Time    `json:"createdAt"`
}

type Service struct {
	mu              sync.RWMutex
	reports         map[string][]*Report // listingID -> reports
	listingsService *listings.Service
}

func NewService(listingsService *listings.Service) *Service {
	return &Service{
		reports:         make(map[string][]*Report),
		listingsService: listingsService,
	}
}

func generateReportID() string {
	b := make([]byte, 4)
	_, _ = rand.Read(b)
	return fmt.Sprintf("rep-%s", hex.EncodeToString(b))
}

func (s *Service) SubmitReport(listingID string, reporterID string, reason ReportReason, comment string) (*Report, error) {
	cleanListingID := strings.TrimSpace(listingID)
	cleanReporterID := strings.TrimSpace(reporterID)
	cleanReason := ReportReason(strings.ToUpper(strings.TrimSpace(string(reason))))

	if cleanReason == "" {
		return nil, ErrEmptyReason
	}
	if !IsValidReason(cleanReason) {
		return nil, ErrInvalidReason
	}

	if s.listingsService != nil {
		item, err := s.listingsService.GetListing(cleanListingID)
		if err != nil {
			return nil, ErrListingNotFound
		}
		if item.SellerID == cleanReporterID {
			return nil, ErrSelfReporting
		}
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	rep := &Report{
		ID:         generateReportID(),
		ListingID:  cleanListingID,
		ReporterID: cleanReporterID,
		Reason:     cleanReason,
		Comment:    strings.TrimSpace(comment),
		CreatedAt:  time.Now().UTC(),
	}

	s.reports[cleanListingID] = append(s.reports[cleanListingID], rep)

	// If listing reaches report threshold (>= 2 reports), automatically hide from feed
	if len(s.reports[cleanListingID]) >= 2 && s.listingsService != nil {
		_ = s.listingsService.SetSystemStatus(cleanListingID, listings.StatusHidden)
	}

	return rep, nil
}

func (s *Service) GetReports(listingID string) ([]*Report, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanListingID := strings.TrimSpace(listingID)
	reps, exists := s.reports[cleanListingID]
	if !exists {
		return []*Report{}, nil
	}

	result := make([]*Report, len(reps))
	copy(result, reps)
	return result, nil
}

func (s *Service) GetReportCount(listingID string) int {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanListingID := strings.TrimSpace(listingID)
	return len(s.reports[cleanListingID])
}
