package handlers_test

import (
	"bytes"
	"encoding/json"
	"image"
	"image/color"
	"image/jpeg"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"

	"goodwill-server/internal/handlers"
)

func createTestImageBytes(t *testing.T) []byte {
	img := image.NewRGBA(image.Rect(0, 0, 10, 10))
	for x := 0; x < 10; x++ {
		for y := 0; y < 10; y++ {
			img.Set(x, y, color.RGBA{R: 255, G: 0, B: 0, A: 255})
		}
	}
	var buf bytes.Buffer
	if err := jpeg.Encode(&buf, img, nil); err != nil {
		t.Fatalf("failed to encode test jpeg: %v", err)
	}
	return buf.Bytes()
}

func TestUploadFile_Success(t *testing.T) {
	tmpDir, err := os.MkdirTemp("", "goodwill-upload-test-*")
	if err != nil {
		t.Fatalf("failed to create temp dir: %v", err)
	}
	defer os.RemoveAll(tmpDir)

	handler := handlers.NewUploadHandler(tmpDir)

	imgBytes := createTestImageBytes(t)
	var body bytes.Buffer
	writer := multipart.NewWriter(&body)
	part, err := writer.CreateFormFile("file", "test_item.jpg")
	if err != nil {
		t.Fatalf("failed to create form file: %v", err)
	}
	if _, err := part.Write(imgBytes); err != nil {
		t.Fatalf("failed to write img bytes: %v", err)
	}
	writer.Close()

	req := httptest.NewRequest("POST", "/v1/upload", &body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
	w := httptest.NewRecorder()

	handler.UploadFile(w, req)

	if w.Code != http.StatusCreated {
		t.Fatalf("expected status 201 Created, got %d. Body: %s", w.Code, w.Body.String())
	}

	var resp handlers.UploadResponse
	if err := json.Unmarshal(w.Body.Bytes(), &resp); err != nil {
		t.Fatalf("failed to parse response: %v", err)
	}

	if resp.URL == "" || resp.Filename == "" {
		t.Fatalf("expected non-empty URL and Filename, got URL=%s Filename=%s", resp.URL, resp.Filename)
	}

	// Verify file exists on disk
	savedPath := filepath.Join(tmpDir, resp.Filename)
	if _, err := os.Stat(savedPath); os.IsNotExist(err) {
		t.Fatalf("saved file does not exist at %s", savedPath)
	}
}

func TestUploadFile_RejectNonImage(t *testing.T) {
	tmpDir, err := os.MkdirTemp("", "goodwill-upload-test-*")
	if err != nil {
		t.Fatalf("failed to create temp dir: %v", err)
	}
	defer os.RemoveAll(tmpDir)

	handler := handlers.NewUploadHandler(tmpDir)

	var body bytes.Buffer
	writer := multipart.NewWriter(&body)
	part, err := writer.CreateFormFile("file", "test.txt")
	if err != nil {
		t.Fatalf("failed to create form file: %v", err)
	}
	part.Write([]byte("This is plain text, not an image file."))
	writer.Close()

	req := httptest.NewRequest("POST", "/v1/upload", &body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
	w := httptest.NewRecorder()

	handler.UploadFile(w, req)

	if w.Code != http.StatusBadRequest {
		t.Fatalf("expected status 400 Bad Request for non-image, got %d", w.Code)
	}
}
