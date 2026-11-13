package storage

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestSupabaseStorage_IsConfigured(t *testing.T) {
	s1 := NewSupabaseStorage("", "", "")
	if s1.IsConfigured() {
		t.Errorf("expected not configured when URL and key are empty")
	}

	s2 := NewSupabaseStorage("https://example.supabase.co", "secret_key", "goodwill-images")
	if !s2.IsConfigured() {
		t.Errorf("expected configured when URL and key are present")
	}
}

func TestSupabaseStorage_UploadSuccess(t *testing.T) {
	ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Header.Get("Authorization") != "Bearer test-key" {
			t.Errorf("missing or invalid Authorization header: %s", r.Header.Get("Authorization"))
		}
		if r.Header.Get("apikey") != "test-key" {
			t.Errorf("missing or invalid apikey header: %s", r.Header.Get("apikey"))
		}
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(`{"Key":"goodwill-images/photo1.jpg"}`))
	}))
	defer ts.Close()

	storageClient := NewSupabaseStorage(ts.URL, "test-key", "goodwill-images")
	url, err := storageClient.Upload(context.Background(), "photo1.jpg", "image/jpeg", []byte("fake-image-bytes"))
	if err != nil {
		t.Fatalf("unexpected upload error: %v", err)
	}

	expectedURL := ts.URL + "/storage/v1/object/public/goodwill-images/photo1.jpg"
	if url != expectedURL {
		t.Errorf("expected public URL %s, got %s", expectedURL, url)
	}
}

