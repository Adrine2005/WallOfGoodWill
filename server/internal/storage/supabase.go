package storage

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

// SupabaseStorage provides cloud object storage operations via Supabase Storage REST API.
type SupabaseStorage struct {
	BaseURL    string
	ServiceKey string
	Bucket     string
	HTTPClient *http.Client
}

// NewSupabaseStorage initializes a Supabase Storage client.
func NewSupabaseStorage(baseURL, serviceKey, bucket string) *SupabaseStorage {
	baseURL = strings.TrimRight(baseURL, "/")
	if bucket == "" {
		bucket = "goodwill-images"
	}
	return &SupabaseStorage{
		BaseURL:    baseURL,
		ServiceKey: serviceKey,
		Bucket:     bucket,
		HTTPClient: &http.Client{Timeout: 30 * time.Second},
	}
}

// IsConfigured returns true if the required credentials and base URL are present.
func (s *SupabaseStorage) IsConfigured() bool {
	return s != nil && s.BaseURL != "" && s.ServiceKey != ""
}

// Upload uploads file data to the configured Supabase Storage bucket and returns the public CDN URL.
func (s *SupabaseStorage) Upload(ctx context.Context, filename string, contentType string, data []byte) (string, error) {
	if !s.IsConfigured() {
		return "", fmt.Errorf("supabase storage is not configured")
	}

	cleanFilename := strings.TrimLeft(filename, "/")
	uploadURL := fmt.Sprintf("%s/storage/v1/object/%s/%s", s.BaseURL, s.Bucket, cleanFilename)

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, uploadURL, bytes.NewReader(data))
	if err != nil {
		return "", fmt.Errorf("failed to create upload request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+s.ServiceKey)
	req.Header.Set("apikey", s.ServiceKey)
	if contentType != "" {
		req.Header.Set("Content-Type", contentType)
	} else {
		req.Header.Set("Content-Type", "application/octet-stream")
	}
	req.Header.Set("x-upsert", "true")

	resp, err := s.HTTPClient.Do(req)
	if err != nil {
		return "", fmt.Errorf("failed to dispatch upload to Supabase: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		body, _ := io.ReadAll(resp.Body)
		return "", fmt.Errorf("supabase upload failed with HTTP %d: %s", resp.StatusCode, string(body))
	}

	// Public CDN URL format for public buckets in Supabase:
	// https://<project-ref>.supabase.co/storage/v1/object/public/<bucket>/<filename>
	publicURL := fmt.Sprintf("%s/storage/v1/object/public/%s/%s", s.BaseURL, s.Bucket, cleanFilename)
	return publicURL, nil
}
