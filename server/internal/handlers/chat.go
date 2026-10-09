package handlers

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"

	"github.com/go-chi/chi/v5"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/chat"
)

type ChatHandler struct {
	chat *chat.Service
}

func NewChatHandler(c *chat.Service) *ChatHandler {
	return &ChatHandler{
		chat: c,
	}
}

type CreateConversationRequest struct {
	ListingID string `json:"listingId"`
}

type SendMessageRequest struct {
	Text string `json:"text"`
}

func (h *ChatHandler) CreateOrGetConversation(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req CreateConversationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	if strings.TrimSpace(req.ListingID) == "" {
		writeError(w, http.StatusBadRequest, "listingId is required")
		return
	}

	conv, err := h.chat.GetOrCreateConversation(req.ListingID, claims.UID)
	if err != nil {
		if errors.Is(err, chat.ErrListingNotFound) {
			writeError(w, http.StatusNotFound, "listing not found")
			return
		}
		if errors.Is(err, chat.ErrSelfChat) {
			writeError(w, http.StatusBadRequest, "cannot start a conversation on your own listing")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusOK, conv)
}

func (h *ChatHandler) GetConversations(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	list := h.chat.GetConversations(claims.UID)
	if list == nil {
		list = make([]*chat.Conversation, 0)
	}

	writeJSON(w, http.StatusOK, list)
}

func (h *ChatHandler) GetConversation(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	convID := chi.URLParam(r, "id")
	if strings.TrimSpace(convID) == "" {
		writeError(w, http.StatusBadRequest, "conversation id is required")
		return
	}

	conv, err := h.chat.GetConversation(convID, claims.UID)
	if err != nil {
		if errors.Is(err, chat.ErrConversationNotFound) {
			writeError(w, http.StatusNotFound, "conversation not found")
			return
		}
		if errors.Is(err, chat.ErrUnauthorized) {
			writeError(w, http.StatusForbidden, "unauthorized: not a participant")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusOK, conv)
}

func (h *ChatHandler) GetMessages(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	convID := chi.URLParam(r, "id")
	if strings.TrimSpace(convID) == "" {
		writeError(w, http.StatusBadRequest, "conversation id is required")
		return
	}

	messages, err := h.chat.GetMessages(convID, claims.UID)
	if err != nil {
		if errors.Is(err, chat.ErrConversationNotFound) {
			writeError(w, http.StatusNotFound, "conversation not found")
			return
		}
		if errors.Is(err, chat.ErrUnauthorized) {
			writeError(w, http.StatusForbidden, "unauthorized: not a participant")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	if messages == nil {
		messages = make([]*chat.ChatMessage, 0)
	}

	writeJSON(w, http.StatusOK, messages)
}

func (h *ChatHandler) SendMessage(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	convID := chi.URLParam(r, "id")
	if strings.TrimSpace(convID) == "" {
		writeError(w, http.StatusBadRequest, "conversation id is required")
		return
	}

	var req SendMessageRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	if strings.TrimSpace(req.Text) == "" {
		writeError(w, http.StatusBadRequest, "message text cannot be empty")
		return
	}

	msg, err := h.chat.SendMessage(convID, claims.UID, req.Text)
	if err != nil {
		if errors.Is(err, chat.ErrConversationNotFound) {
			writeError(w, http.StatusNotFound, "conversation not found")
			return
		}
		if errors.Is(err, chat.ErrUnauthorized) {
			writeError(w, http.StatusForbidden, "unauthorized: not a participant")
			return
		}
		if errors.Is(err, chat.ErrEmptyMessage) {
			writeError(w, http.StatusBadRequest, "message text cannot be empty")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusCreated, msg)
}
