# Spec Delta

## ADDED Requirements

### Requirement: Relational Database Persistence
The backend service SHALL support persistent relational storage in a PostgreSQL database when configured via the DATABASE_URL environment variable, executing schema migrations on startup, and falling back gracefully to in-memory storage when DATABASE_URL is not set.

#### Scenario: Server startup with PostgreSQL configuration
- **WHEN** the backend server starts with a valid DATABASE_URL pointing to a PostgreSQL instance
- **THEN** the server connects to the database, executes pending schema migrations, and routes data operations to PostgreSQL

#### Scenario: Server startup in local development fallback mode
- **WHEN** the backend server starts without a DATABASE_URL environment variable
- **THEN** the server operates using in-memory data structures, preserving local offline development workflows

### Requirement: Cloud Object Storage for Uploaded Media
The backend service SHALL support uploading multipart images to a Supabase Storage bucket when configured with Supabase credentials, returning public CDN URLs, and falling back to local disk storage when cloud credentials are omitted.

#### Scenario: Image upload with Supabase Storage configured
- **WHEN** a client uploads an image file and SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY are set
- **THEN** the server uploads the file to the configured Supabase Storage bucket and returns HTTP 200 with the public HTTPS CDN URL

#### Scenario: Image upload in local storage fallback mode
- **WHEN** a client uploads an image file and Supabase credentials are not provided
- **THEN** the server stores the file on the local filesystem in the uploads directory and returns a local relative URL

### Requirement: Containerized Service Deployment
The backend service repository SHALL provide a multi-stage Dockerfile and deployment manifest compatible with free container hosting platforms.

#### Scenario: Container image compilation
- **WHEN** a container builder builds the server Dockerfile
- **THEN** a minimal, non-root Linux container image containing the compiled Go binary is generated successfully

