package config

import (
	"os"
)

type Config struct {
	Port           string
	Env            string
	DatabaseURL    string
	SupabaseURL    string
	SupabaseKey    string
	SupabaseBucket string
}

func Load() *Config {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	env := os.Getenv("ENV")
	if env == "" {
		env = "development"
	}

	databaseURL := os.Getenv("DATABASE_URL")

	supabaseURL := os.Getenv("SUPABASE_URL")
	supabaseKey := os.Getenv("SUPABASE_SERVICE_ROLE_KEY")
	if supabaseKey == "" {
		supabaseKey = os.Getenv("SUPABASE_KEY")
	}

	supabaseBucket := os.Getenv("SUPABASE_STORAGE_BUCKET")
	if supabaseBucket == "" {
		supabaseBucket = "goodwill-images"
	}

	return &Config{
		Port:           port,
		Env:            env,
		DatabaseURL:    databaseURL,
		SupabaseURL:    supabaseURL,
		SupabaseKey:    supabaseKey,
		SupabaseBucket: supabaseBucket,
	}
}
