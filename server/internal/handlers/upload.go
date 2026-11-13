package handlers

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"io"
	"log"
	"mime"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"goodwill-server/internal/storage"
)

type UploadHandler struct {
	uploadDir string
	supabase  *storage.SupabaseStorage
}

func NewUploadHandler(uploadDir string, supabase ...*storage.SupabaseStorage) *UploadHandler {
	if uploadDir == "" {
		uploadDir = filepath.Join(".", "uploads")
	}
	_ = os.MkdirAll(uploadDir, 0755)

	var sClient *storage.SupabaseStorage
	if len(supabase) > 0 {
		sClient = supabase[0]
	}

	return &UploadHandler{
		uploadDir: uploadDir,
		supabase:  sClient,
	}
}

type UploadResponse struct {
	URL      string   `json:"url"`
	Filename string   `json:"filename"`
	URLs     []string `json:"urls,omitempty"`
}

func (h *UploadHandler) UploadFile(w http.ResponseWriter, r *http.Request) {
	// 10MB max upload size
	const maxUploadSize = 10 << 20
	r.Body = http.MaxBytesReader(w, r.Body, maxUploadSize)

	if err := r.ParseMultipartForm(maxUploadSize); err != nil {
		writeError(w, http.StatusBadRequest, "file size exceeds 10MB limit")
		return
	}

	if r.MultipartForm == nil || len(r.MultipartForm.File) == 0 {
		writeError(w, http.StatusBadRequest, "no image file provided")
		return
	}

	var savedURLs []string
	var firstFilename string

	// Look for files under "file", "photo", "image", or iterate all form file keys
	for _, fileHeaders := range r.MultipartForm.File {
		for _, fileHeader := range fileHeaders {
			file, err := fileHeader.Open()
			if err != nil {
				continue
			}

			// Read file content
			fileBytes, readErr := io.ReadAll(file)
			file.Close()
			if readErr != nil {
				continue
			}

			if len(fileBytes) == 0 {
				continue
			}

			// Detect MIME type
			contentType := http.DetectContentType(fileBytes)
			if !strings.HasPrefix(contentType, "image/") {
				writeError(w, http.StatusBadRequest, "only image files (JPEG, PNG, WEBP, GIF) are allowed")
				return
			}

			ext := filepath.Ext(fileHeader.Filename)
			if ext == "" {
				exts, _ := mime.ExtensionsByType(contentType)
				if len(exts) > 0 {
					ext = exts[0]
				} else {
					ext = ".jpg"
				}
			}

			b := make([]byte, 8)
			_, _ = rand.Read(b)
			generatedName := fmt.Sprintf("img_%d_%s%s", time.Now().Unix(), hex.EncodeToString(b), strings.ToLower(ext))

			var targetURL string

			// 1. Attempt Supabase Cloud Storage if configured
			if h.supabase != nil && h.supabase.IsConfigured() {
				cloudURL, uploadErr := h.supabase.Upload(r.Context(), generatedName, contentType, fileBytes)
				if uploadErr == nil {
					targetURL = cloudURL
				} else {
					log.Printf("⚠️ Supabase Storage upload failed: %v. Falling back to local disk.", uploadErr)
				}
			}

			// 2. Fall back to local disk storage if cloud upload wasn't used or failed
			if targetURL == "" {
				destPath := filepath.Join(h.uploadDir, generatedName)
				if writeErr := os.WriteFile(destPath, fileBytes, 0644); writeErr != nil {
					writeError(w, http.StatusInternalServerError, "failed to save uploaded file locally")
					return
				}
				targetURL = fmt.Sprintf("/uploads/%s", generatedName)
			}

			savedURLs = append(savedURLs, targetURL)
			if firstFilename == "" {
				firstFilename = generatedName
			}
		}
	}

	if len(savedURLs) == 0 {
		writeError(w, http.StatusBadRequest, "no valid image could be uploaded")
		return
	}

	resp := UploadResponse{
		URL:      savedURLs[0],
		Filename: firstFilename,
		URLs:     savedURLs,
	}

	writeJSON(w, http.StatusCreated, resp)
}
