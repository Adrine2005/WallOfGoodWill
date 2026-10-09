package handlers

import (
	"errors"
	"net/http"
	"strings"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/notifications"
)

type NotificationsHandler struct {
	notifications *notifications.Service
}

func NewNotificationsHandler(n *notifications.Service) *NotificationsHandler {
	return &NotificationsHandler{
		notifications: n,
	}
}

func (h *NotificationsHandler) GetNotifications(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	list := h.notifications.GetNotifications(claims.UID)
	if list == nil {
		list = make([]*notifications.Notification, 0)
	}

	writeJSON(w, http.StatusOK, list)
}

func (h *NotificationsHandler) GetUnreadCount(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	count := h.notifications.GetUnreadCount(claims.UID)
	writeJSON(w, http.StatusOK, map[string]int{"count": count})
}

func (h *NotificationsHandler) MarkAsRead(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	notifID := chi.URLParam(r, "id")
	if strings.TrimSpace(notifID) == "" {
		writeError(w, http.StatusBadRequest, "notification id is required")
		return
	}

	notif, err := h.notifications.MarkAsRead(notifID, claims.UID)
	if err != nil {
		switch {
		case errors.Is(err, notifications.ErrNotificationNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, notifications.ErrUnauthorized):
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusBadRequest, err.Error())
		}
		return
	}

	writeJSON(w, http.StatusOK, notif)
}

func (h *NotificationsHandler) MarkAllAsRead(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	count := h.notifications.MarkAllAsRead(claims.UID)
	writeJSON(w, http.StatusOK, map[string]int{"marked": count})
}
