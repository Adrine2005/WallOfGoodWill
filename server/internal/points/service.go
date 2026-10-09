package points

import (
	"sort"
	"strings"
	"sync"
	"time"

	"goodwill-server/internal/users"
)

type Badge struct {
	ID           string `json:"id"`
	Name         string `json:"name"`
	Description  string `json:"description"`
	Icon         string `json:"icon"`
	MinPoints    int    `json:"minPoints"`
	MinDonations int    `json:"minDonations"`
}

var AllBadges = []Badge{
	{
		ID:           "donor-pioneer",
		Name:         "Donor Pioneer",
		Description:  "Donated your first item on campus",
		Icon:         "🌱",
		MinPoints:    50,
		MinDonations: 1,
	},
	{
		ID:           "campus-philanthropist",
		Name:         "Campus Philanthropist",
		Description:  "Completed 3+ hostel donations",
		Icon:         "🎖️",
		MinPoints:    150,
		MinDonations: 3,
	},
	{
		ID:           "eco-champion",
		Name:         "Eco Champion",
		Description:  "Completed 5+ donations, promoting zero waste",
		Icon:         "🏆",
		MinPoints:    250,
		MinDonations: 5,
	},
}

type UserScore struct {
	UserID        string    `json:"userId"`
	UserName      string    `json:"userName"`
	HostelBlock   string    `json:"hostelBlock"`
	Points        int       `json:"points"`
	DonationCount int       `json:"donationCount"`
	UpdatedAt     time.Time `json:"updatedAt"`
}

type UserPoints struct {
	UserID          string    `json:"userId"`
	UserName        string    `json:"userName"`
	HostelBlock     string    `json:"hostelBlock"`
	Points          int       `json:"points"`
	DonationCount   int       `json:"donationCount"`
	TierName        string    `json:"tierName"`
	NextTierPoints  int       `json:"nextTierPoints"`
	ProgressPercent int       `json:"progressPercent"`
	Badges          []Badge   `json:"badges"`
	UpdatedAt       time.Time `json:"updatedAt"`
}

type LeaderboardEntry struct {
	Rank          int    `json:"rank"`
	UserID        string `json:"userId"`
	UserName      string `json:"userName"`
	HostelBlock   string `json:"hostelBlock"`
	Points        int    `json:"points"`
	DonationCount int    `json:"donationCount"`
	TierName      string `json:"tierName"`
	BadgeIcon     string `json:"badgeIcon"`
}

type Service struct {
	mu           sync.RWMutex
	scores       map[string]*UserScore
	usersService *users.Service
}

func NewService(u *users.Service) *Service {
	s := &Service{
		scores:       make(map[string]*UserScore),
		usersService: u,
	}
	s.seedData()
	return s
}

func (s *Service) seedData() {
	now := time.Now().UTC()
	seeds := []*UserScore{
		{
			UserID:        "user-ananya",
			UserName:      "Ananya Sen",
			HostelBlock:   "Hostel B (Girls)",
			Points:        350,
			DonationCount: 7,
			UpdatedAt:     now.Add(-24 * time.Hour),
		},
		{
			UserID:        "user-rahul",
			UserName:      "Rahul Verma",
			HostelBlock:   "Hostel A (Boys)",
			Points:        200,
			DonationCount: 4,
			UpdatedAt:     now.Add(-12 * time.Hour),
		},
		{
			UserID:        "user-kavya",
			UserName:      "Kavya Nair",
			HostelBlock:   "Hostel C (Girls)",
			Points:        100,
			DonationCount: 2,
			UpdatedAt:     now.Add(-6 * time.Hour),
		},
	}

	for _, sc := range seeds {
		s.scores[sc.UserID] = sc
	}
}

func computeTierAndBadges(points, donations int) (string, int, int, []Badge) {
	var earned []Badge
	for _, b := range AllBadges {
		if points >= b.MinPoints || donations >= b.MinDonations {
			earned = append(earned, b)
		}
	}

	var tierName string
	var nextTierPoints int
	var progress int

	if points >= 250 || donations >= 5 {
		tierName = "Eco Champion"
		nextTierPoints = 250
		progress = 100
	} else if points >= 150 || donations >= 3 {
		tierName = "Campus Philanthropist"
		nextTierPoints = 250
		progress = int(float64(points-150) / 100.0 * 100)
	} else if points >= 50 || donations >= 1 {
		tierName = "Donor Pioneer"
		nextTierPoints = 150
		progress = int(float64(points-50) / 100.0 * 100)
	} else {
		tierName = "Newcomer"
		nextTierPoints = 50
		progress = int(float64(points) / 50.0 * 100)
	}

	if progress > 100 {
		progress = 100
	}
	if progress < 0 {
		progress = 0
	}

	return tierName, nextTierPoints, progress, earned
}

func (s *Service) GetUserPoints(userID string) *UserPoints {
	s.mu.RLock()
	defer s.mu.RUnlock()

	cleanID := strings.TrimSpace(userID)
	score, exists := s.scores[cleanID]

	userName := cleanID
	hostelBlock := "Campus"
	if s.usersService != nil {
		u, _ := s.usersService.GetUser(cleanID)
		if u != nil {
			userName = u.Name
			if u.HostelBlock != "" {
				hostelBlock = u.HostelBlock
			}
		}
	}

	pts := 0
	donations := 0
	updated := time.Now().UTC()

	if exists {
		pts = score.Points
		donations = score.DonationCount
		if score.UserName != "" {
			userName = score.UserName
		}
		if score.HostelBlock != "" {
			hostelBlock = score.HostelBlock
		}
		updated = score.UpdatedAt
	}

	tier, nextTier, prog, badges := computeTierAndBadges(pts, donations)

	return &UserPoints{
		UserID:          cleanID,
		UserName:        userName,
		HostelBlock:     hostelBlock,
		Points:          pts,
		DonationCount:   donations,
		TierName:        tier,
		NextTierPoints:  nextTier,
		ProgressPercent: prog,
		Badges:          badges,
		UpdatedAt:       updated,
	}
}

func (s *Service) AwardDonationPoints(userID string) *UserPoints {
	s.mu.Lock()
	defer s.mu.Unlock()

	cleanID := strings.TrimSpace(userID)
	score, exists := s.scores[cleanID]
	now := time.Now().UTC()

	if !exists {
		name := cleanID
		hostel := "Campus"
		if s.usersService != nil {
			u, _ := s.usersService.GetUser(cleanID)
			if u != nil {
				name = u.Name
				if u.HostelBlock != "" {
					hostel = u.HostelBlock
				}
			}
		}
		score = &UserScore{
			UserID:        cleanID,
			UserName:      name,
			HostelBlock:   hostel,
			Points:        0,
			DonationCount: 0,
			UpdatedAt:     now,
		}
		s.scores[cleanID] = score
	}

	score.Points += 50
	score.DonationCount += 1
	score.UpdatedAt = now

	tier, nextTier, prog, badges := computeTierAndBadges(score.Points, score.DonationCount)

	return &UserPoints{
		UserID:          cleanID,
		UserName:        score.UserName,
		HostelBlock:     score.HostelBlock,
		Points:          score.Points,
		DonationCount:   score.DonationCount,
		TierName:        tier,
		NextTierPoints:  nextTier,
		ProgressPercent: prog,
		Badges:          badges,
		UpdatedAt:       now,
	}
}

func (s *Service) GetLeaderboard() []*LeaderboardEntry {
	s.mu.RLock()
	defer s.mu.RUnlock()

	var list []*UserScore
	for _, sc := range s.scores {
		list = append(list, sc)
	}

	sort.Slice(list, func(i, j int) bool {
		if list[i].Points == list[j].Points {
			return list[i].DonationCount > list[j].DonationCount
		}
		return list[i].Points > list[j].Points
	})

	var result []*LeaderboardEntry
	for i, sc := range list {
		tier, _, _, badges := computeTierAndBadges(sc.Points, sc.DonationCount)
		badgeIcon := "🌱"
		if len(badges) > 0 {
			badgeIcon = badges[len(badges)-1].Icon
		}

		result = append(result, &LeaderboardEntry{
			Rank:          i + 1,
			UserID:        sc.UserID,
			UserName:      sc.UserName,
			HostelBlock:   sc.HostelBlock,
			Points:        sc.Points,
			DonationCount: sc.DonationCount,
			TierName:      tier,
			BadgeIcon:     badgeIcon,
		})
	}

	return result
}
