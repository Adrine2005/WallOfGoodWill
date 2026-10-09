package chat

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
	"goodwill-server/internal/notifications"
	"goodwill-server/internal/users"
)

var (
	ErrConversationNotFound = errors.New("conversation not found")
	ErrListingNotFound      = errors.New("listing not found")
	ErrSelfChat             = errors.New("cannot start a conversation with yourself")
	ErrUnauthorized         = errors.New("unauthorized: not a participant in this conversation")
	ErrEmptyMessage         = errors.New("message text cannot be empty")
)

type Conversation struct {
	ID              string     `json:"id"`
	ListingID       string     `json:"listingId"`
	ListingTitle    string     `json:"listingTitle"`
	BuyerID         string     `json:"buyerId"`
	BuyerName       string     `json:"buyerName"`
	BuyerEmail      string     `json:"buyerEmail"`
	SellerID        string     `json:"sellerId"`
	SellerName      string     `json:"sellerName"`
	SellerEmail     string     `json:"sellerEmail"`
	LastMessageText string     `json:"lastMessageText"`
	LastMessageAt   *time.Time `json:"lastMessageAt,omitempty"`
	CreatedAt       time.Time  `json:"createdAt"`
	UpdatedAt       time.Time  `json:"updatedAt"`
}

type ChatMessage struct {
	ID             string    `json:"id"`
	ConversationID string    `json:"conversationId"`
	SenderID       string    `json:"senderId"`
	SenderName     string    `json:"senderName"`
	SenderEmail    string    `json:"senderEmail"`
	Text           string    `json:"text"`
	CreatedAt      time.Time `json:"createdAt"`
}

type Service struct {
	mu                   sync.RWMutex
	conversations        map[string]*Conversation
	messages             map[string][]*ChatMessage
	listingsService      *listings.Service
	usersService         *users.Service
	notificationsService *notifications.Service
}

func NewService(l *listings.Service, u *users.Service, n *notifications.Service) *Service {
	return &Service{
		conversations:        make(map[string]*Conversation),
		messages:             make(map[string][]*ChatMessage),
		listingsService:      l,
		usersService:         u,
		notificationsService: n,
	}
}

func generateID(prefix string) string {
	b := make([]byte, 4)
	_, _ = rand.Read(b)
	return fmt.Sprintf("%s-%s", prefix, hex.EncodeToString(b))
}

func (s *Service) GetOrCreateConversation(listingID, buyerID string) (*Conversation, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleanListingID := strings.TrimSpace(listingID)
	cleanBuyerID := strings.TrimSpace(buyerID)

	listing, err := s.listingsService.GetListing(cleanListingID)
	if err != nil {
		return nil, ErrListingNotFound
	}

	if listing.SellerID == cleanBuyerID {
		return nil, ErrSelfChat
	}

	// Check if conversation already exists for this listing and buyer
	for _, conv := range s.conversations {
		if conv.ListingID == cleanListingID && conv.BuyerID == cleanBuyerID {
			return conv, nil
		}
	}

	buyer, _ := s.usersService.GetUser(cleanBuyerID)
	seller, _ := s.usersService.GetUser(listing.SellerID)

	buyerName := cleanBuyerID
	buyerEmail := ""
	if buyer != nil {
		buyerName = buyer.Name
		buyerEmail = buyer.Email
	}

	sellerName := listing.SellerID
	sellerEmail := ""
	if seller != nil {
		sellerName = seller.Name
		sellerEmail = seller.Email
	}

	now := time.Now().UTC()
	conv := &Conversation{
		ID:           generateID("conv"),
		ListingID:    listing.ID,
		ListingTitle: listing.Title,
		BuyerID:      cleanBuyerID,
		BuyerName:    buyerName,
		BuyerEmail:   buyerEmail,
		SellerID:     listing.SellerID,
		SellerName:   sellerName,
		SellerEmail:  sellerEmail,
		CreatedAt:    now,
		UpdatedAt:    now,
	}

	s.conversations[conv.ID] = conv
	s.messages[conv.ID] = make([]*ChatMessage, 0)
	return conv, nil
}

func (s *Service) GetConversations(userID string) []*Conversation {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanUserID := strings.TrimSpace(userID)
	var list []*Conversation
	for _, c := range s.conversations {
		if c.BuyerID == cleanUserID || c.SellerID == cleanUserID {
			list = append(list, c)
		}
	}

	sort.Slice(list, func(i, j int) bool {
		return list[i].UpdatedAt.After(list[j].UpdatedAt)
	})

	return list
}

func (s *Service) GetConversation(conversationID, userID string) (*Conversation, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanConvID := strings.TrimSpace(conversationID)
	cleanUserID := strings.TrimSpace(userID)

	conv, exists := s.conversations[cleanConvID]
	if !exists {
		return nil, ErrConversationNotFound
	}

	if conv.BuyerID != cleanUserID && conv.SellerID != cleanUserID {
		return nil, ErrUnauthorized
	}

	return conv, nil
}

func (s *Service) GetMessages(conversationID, userID string) ([]*ChatMessage, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanConvID := strings.TrimSpace(conversationID)
	cleanUserID := strings.TrimSpace(userID)

	conv, exists := s.conversations[cleanConvID]
	if !exists {
		return nil, ErrConversationNotFound
	}

	if conv.BuyerID != cleanUserID && conv.SellerID != cleanUserID {
		return nil, ErrUnauthorized
	}

	msgs := s.messages[cleanConvID]
	result := make([]*ChatMessage, len(msgs))
	copy(result, msgs)

	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.Before(result[j].CreatedAt)
	})

	return result, nil
}

func (s *Service) SendMessage(conversationID, senderID, text string) (*ChatMessage, error) {
	cleanText := strings.TrimSpace(text)
	if cleanText == "" {
		return nil, ErrEmptyMessage
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	cleanConvID := strings.TrimSpace(conversationID)
	cleanSenderID := strings.TrimSpace(senderID)

	conv, exists := s.conversations[cleanConvID]
	if !exists {
		return nil, ErrConversationNotFound
	}

	if conv.BuyerID != cleanSenderID && conv.SellerID != cleanSenderID {
		return nil, ErrUnauthorized
	}

	var recipientID string
	if cleanSenderID == conv.BuyerID {
		recipientID = conv.SellerID
	} else {
		recipientID = conv.BuyerID
	}

	sender, _ := s.usersService.GetUser(cleanSenderID)
	senderName := cleanSenderID
	senderEmail := ""
	if sender != nil {
		senderName = sender.Name
		senderEmail = sender.Email
	}

	now := time.Now().UTC()
	msg := &ChatMessage{
		ID:             generateID("msg"),
		ConversationID: conv.ID,
		SenderID:       cleanSenderID,
		SenderName:     senderName,
		SenderEmail:    senderEmail,
		Text:           cleanText,
		CreatedAt:      now,
	}

	s.messages[conv.ID] = append(s.messages[conv.ID], msg)
	conv.LastMessageText = cleanText
	conv.LastMessageAt = &now
	conv.UpdatedAt = now

	// Trigger in-app notification to recipient
	if s.notificationsService != nil {
		notifTitle := fmt.Sprintf("Message on %s", conv.ListingTitle)
		notifMsg := fmt.Sprintf("%s: %s", senderName, cleanText)
		s.notificationsService.CreateNotification(recipientID, notifTitle, notifMsg, notifications.TypeChatMessage, conv.ID)
	}

	return msg, nil
}
