package db

import (
	"context"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
)

// DB wraps the PostgreSQL connection pool.
type DB struct {
	Pool *pgxpool.Pool
}

// Connect initializes a connection pool to PostgreSQL using pgxpool.
func Connect(ctx context.Context, databaseURL string) (*DB, error) {
	if databaseURL == "" {
		return nil, fmt.Errorf("DATABASE_URL is empty")
	}

	config, err := pgxpool.ParseConfig(databaseURL)
	if err != nil {
		return nil, fmt.Errorf("failed to parse DATABASE_URL: %w", err)
	}

	config.MaxConns = 15
	config.MinConns = 2
	config.MaxConnLifetime = 1 * time.Hour
	config.MaxConnIdleTime = 15 * time.Minute

	pool, err := pgxpool.NewWithConfig(ctx, config)
	if err != nil {
		return nil, fmt.Errorf("failed to create connection pool: %w", err)
	}

	// Ping database to verify credentials & network route
	pingCtx, cancel := context.WithTimeout(ctx, 8*time.Second)
	defer cancel()
	if err := pool.Ping(pingCtx); err != nil {
		pool.Close()
		return nil, fmt.Errorf("failed to ping database: %w", err)
	}

	log.Println("✓ Successfully connected to PostgreSQL database")
	return &DB{Pool: pool}, nil
}

// RunMigrations executes a given SQL file against the connected database pool.
func (d *DB) RunMigrations(ctx context.Context, migrationPath string) error {
	content, err := os.ReadFile(migrationPath)
	if err != nil {
		return fmt.Errorf("failed to read migration file %s: %w", migrationPath, err)
	}

	_, err = d.Pool.Exec(ctx, string(content))
	if err != nil {
		return fmt.Errorf("failed to execute migration %s: %w", migrationPath, err)
	}

	log.Printf("✓ Successfully applied database migration: %s", filepath.Base(migrationPath))
	return nil
}

// Close gracefully terminates all connections in the pool.
func (d *DB) Close() {
	if d.Pool != nil {
		d.Pool.Close()
	}
}
