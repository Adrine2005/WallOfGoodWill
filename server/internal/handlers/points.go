package handlers

import (
	"net/http"

	"goodwill-server/internal/auth"
	"goodwill-server/internal/points"
)

type PointsHandler struct {
	points *points.Service
}

func NewPointsHandler(p *points.Service) *PointsHandler {
	return &PointsHandler{
		points: p,
	}
}

func (h *PointsHandler) GetMyPoints(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	res := h.points.GetUserPoints(claims.UID)
	writeJSON(w, http.StatusOK, res)
}

func (h *PointsHandler) GetLeaderboard(w http.ResponseWriter, r *http.Request) {
	claims, ok := auth.GetUserClaims(r.Context())
	if !ok || claims.UID == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	list := h.points.GetLeaderboard()
	if list == nil {
		list = make([]*points.LeaderboardEntry, 0)
	}

	writeJSON(w, http.StatusOK, list)
}
