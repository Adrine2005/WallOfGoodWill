package listings_test

import (
	"errors"
	"testing"

	"goodwill-server/internal/listings"
)

func TestService_SeedListings(t *testing.T) {
	svc := listings.NewService()
	items := svc.ListListings(listings.Filter{})

	if len(items) < 4 {
		t.Fatalf("expected at least 4 seed listings, got %d", len(items))
	}
}

func TestService_CreateDonationListing(t *testing.T) {
	svc := listings.NewService()

	item, err := svc.CreateListing(
		"student-1",
		"Test User",
		"Block A",
		"101",
		"Algorithms Textbook",
		"CLRS book in good condition",
		listings.CategoryBooks,
		listings.TypeDonate,
		500, // Should be overridden to 0 for DONATE
		listings.ConditionGood,
		[]string{"https://example.com/photo.jpg"},
	)

	if err != nil {
		t.Fatalf("failed to create donation: %v", err)
	}

	if item.Price != 0 {
		t.Errorf("expected donation price to be 0, got %f", item.Price)
	}
	if item.Type != listings.TypeDonate {
		t.Errorf("expected type DONATE, got %s", item.Type)
	}
	if item.Status != listings.StatusActive {
		t.Errorf("expected status ACTIVE, got %s", item.Status)
	}
}

func TestService_CreateSaleListing(t *testing.T) {
	svc := listings.NewService()

	// Valid sale
	item, err := svc.CreateListing(
		"student-2",
		"Seller",
		"Block B",
		"202",
		"Desk Fan",
		"High speed fan",
		listings.CategoryElectronics,
		listings.TypeSell,
		350,
		listings.ConditionLikeNew,
		nil,
	)
	if err != nil {
		t.Fatalf("failed to create sale: %v", err)
	}
	if item.Price != 350 {
		t.Errorf("expected price 350, got %f", item.Price)
	}

	// Invalid sale price <= 0
	_, errInvalid := svc.CreateListing(
		"student-2",
		"Seller",
		"Block B",
		"202",
		"Invalid Fan",
		"Description",
		listings.CategoryElectronics,
		listings.TypeSell,
		0,
		listings.ConditionLikeNew,
		nil,
	)
	if !errors.Is(errInvalid, listings.ErrInvalidPrice) {
		t.Errorf("expected ErrInvalidPrice, got %v", errInvalid)
	}
}

func TestService_Filtering(t *testing.T) {
	svc := listings.NewService()

	// Filter by Category BOOKS
	books := svc.ListListings(listings.Filter{Category: listings.CategoryBooks})
	for _, b := range books {
		if b.Category != listings.CategoryBooks {
			t.Errorf("expected category BOOKS, got %s", b.Category)
		}
	}

	// Filter by Type DONATE
	donations := svc.ListListings(listings.Filter{Type: listings.TypeDonate})
	for _, d := range donations {
		if d.Type != listings.TypeDonate {
			t.Errorf("expected type DONATE, got %s", d.Type)
		}
	}

	// Search keyword
	matches := svc.ListListings(listings.Filter{Search: "kettle"})
	if len(matches) != 1 {
		t.Errorf("expected 1 match for 'kettle', got %d", len(matches))
	}
}

func TestService_DeleteListing(t *testing.T) {
	svc := listings.NewService()

	item, err := svc.CreateListing(
		"student-own",
		"Owner",
		"Block C",
		"303",
		"Lamp",
		"Desk lamp",
		listings.CategoryElectronics,
		listings.TypeDonate,
		0,
		listings.ConditionGood,
		nil,
	)
	if err != nil {
		t.Fatalf("failed to create item: %v", err)
	}

	// Unauthorized delete
	errUnauth := svc.DeleteListing(item.ID, "student-other")
	if !errors.Is(errUnauth, listings.ErrUnauthorized) {
		t.Errorf("expected ErrUnauthorized, got %v", errUnauth)
	}

	// Authorized delete
	errAuth := svc.DeleteListing(item.ID, "student-own")
	if errAuth != nil {
		t.Errorf("unexpected error on delete: %v", errAuth)
	}

	// Verify it's no longer found
	_, errNotFound := svc.GetListing(item.ID)
	if !errors.Is(errNotFound, listings.ErrListingNotFound) {
		t.Errorf("expected ErrListingNotFound, got %v", errNotFound)
	}
}
