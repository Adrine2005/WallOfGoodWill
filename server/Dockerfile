# Stage 1: Build the static Go binary
FROM golang:alpine AS builder

WORKDIR /app

# Cache Go modules
COPY go.mod go.sum ./
RUN go mod download

# Copy application source code and SQL migrations
COPY . .

# Build statically linked Go binary without CGO
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-w -s" -o /app/bin/server ./cmd/api

# Stage 2: Minimal hardened runtime image (<25MB)
FROM alpine:3.20

WORKDIR /app

# Copy SSL CA certificates from builder to enable outgoing HTTPS calls to Supabase API
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt

# Copy binary and migrations from builder
COPY --from=builder /app/bin/server /app/server
COPY --from=builder /app/migrations /app/migrations

# Create uploads directory for fallback local storage
RUN mkdir -p /app/uploads

# Expose default application port
EXPOSE 8080

# Default environment configuration
ENV PORT=8080 \
    ENV=production

# Non-root security user
RUN adduser -D -u 10001 -g '' appuser && chown -R appuser:appuser /app
USER appuser

ENTRYPOINT ["/app/server"]


