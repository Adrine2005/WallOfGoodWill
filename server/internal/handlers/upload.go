package handlers

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"io"
	"mime"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"
)

type UploadHandler struct {
	uploadDir string
}

func NewUploadHandler(uploadDir string) *UploadHandler {
	if uploadDir == "" {
		uploadDir = filepath.Join(".", "uploads")
	}
	_ = os.MkdirAll(uploadDir, 0755)
	return &UploadHandler{uploadDir: uploadDir}
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

			// Read first 512 bytes for MIME detection
			buff := make([]byte, 512)
			n, readErr := file.Read(buff)
			if readErr != nil && readErr != io.EOF {
				file.Close()
				continue
			}

			contentType := http.DetectContentType(buff[:n])
			if !strings.HasPrefix(contentType, "image/") {
				file.Close()
				writeError(w, http.StatusBadRequest, "only image files (JPEG, PNG, WEBP, GIF) are allowed")
				return
			}

			// Reset file read pointer
			if _, seekErr := file.Seek(0, io.SeekStart); seekErr != nil {
				file.Close()
				continue
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
			destPath := filepath.Join(h.uploadDir, generatedName)

			destFile, err := os.Create(destPath)
			if err != nil {
				file.Close()
				writeError(w, http.StatusInternalServerError, "failed to save uploaded file")
				return
			}

			_, copyErr := io.Copy(destFile, file)
			destFile.Close()
			file.Close()

			if copyErr != nil {
				_ = os.Remove(destPath)
				writeError(w, http.StatusInternalServerError, "failed to write file content")
				return
			}

			relURL := fmt.Sprintf("/uploads/%s", generatedName)
			savedURLs = append(savedURLs, relURL)
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
