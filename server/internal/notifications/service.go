package notifications

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"sort"
	"strings"
	"sync"
	"time"
)

var (
	ErrNotificationNotFound = errors.New("notification not found")
	ErrUnauthorized         = errors.New("unauthorized: notification belongs to another user")
)

type NotificationType string

const (
	TypeRequestReceived NotificationType = "REQUEST_RECEIVED"
	TypeRequestAccepted NotificationType = "REQUEST_ACCEPTED"
	TypeRequestDeclined NotificationType = "REQUEST_DECLINED"
)

type Notification struct {
	ID        string           `json:"id"`
	UserID    string           `json:"userId"`
	Title     string           `json:"title"`
	Message   string           `json:"message"`
	Type      NotificationType `json:"type"`
	TargetID  string           `json:"targetId"`
	IsRead    bool             `json:"isRead"`
	CreatedAt time.Time        `json:"createdAt"`
}

type Service struct {
	mu            sync.RWMutex
	notifications map[string]*Notification
}

func NewService() *Service {
	return &Service{
		notifications: make(map[string]*Notification),
	}
}

func generateID() string {
	b := make([]byte, 4)
	_, _ = rand.Read(b)
	return fmt.Sprintf("notif-%s", hex.EncodeToString(b))
}

func (s *Service) CreateNotification(userID, title, message string, notifType NotificationType, targetID string) *Notification {
	s.mu.Lock()
	defer s.mu.Unlock()

	notif := &Notification{
		ID:        generateID(),
		UserID:    strings.TrimSpace(userID),
		Title:     strings.TrimSpace(title),
		Message:   strings.TrimSpace(message),
		Type:      notifType,
		TargetID:  strings.TrimSpace(targetID),
		IsRead:    false,
		CreatedAt: time.Now().UTC(),
	}

	s.notifications[notif.ID] = notif
	return notif
}

func (s *Service) GetNotifications(userID string) []*Notification {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanUser := strings.TrimSpace(userID)
	var result []*Notification
	for _, n := range s.notifications {
		if n.UserID == cleanUser {
			result = append(result, n)
		}
	}

	sort.Slice(result, func(i, j int) bool {
		return result[i].CreatedAt.After(result[j].CreatedAt)
	})

	return result
}

func (s *Service) GetUnreadCount(userID string) int {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanUser := strings.TrimSpace(userID)
	count := 0
	for _, n := range s.notifications {
		if n.UserID == cleanUser && !n.IsRead {
			count++
		}
	}
	return count
}

func (s *Service) MarkAsRead(notificationID, userID string) (*Notification, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	notif, exists := s.notifications[strings.TrimSpace(notificationID)]
	if !exists {
		return nil, ErrNotificationNotFound
	}

	if notif.UserID != strings.TrimSpace(userID) {
		return nil, ErrUnauthorized
	}

	notif.IsRead = true
	return notif, nil
}

func (s *Service) MarkAllAsRead(userID string) int {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleanUser := strings.TrimSpace(userID)
	count := 0
	for _, n := range s.notifications {
		if n.UserID == cleanUser && !n.IsRead {
			n.IsRead = true
			count++
		}
	}
	return count
}
