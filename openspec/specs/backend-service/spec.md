# backend-service Specification

## Purpose

Provides foundational HTTP server infrastructure, configuration management, and health probing for the Wall of Goodwill backend service.

## Requirements

### Requirement: Service Health Reporting
The backend service SHALL expose an unauthenticated health endpoint at GET /health that reports service availability and runtime status in JSON format.

#### Scenario: Successful health check
- **WHEN** a client performs a GET request to /health
- **THEN** the server returns HTTP 200 with a JSON response containing `status: "ok"` and current server timestamp

### Requirement: Environment Configuration
The backend service SHALL load runtime configuration settings from environment variables with sensible local defaults for port, host, and logging.

#### Scenario: Missing optional environment variables
- **WHEN** the backend server starts without custom port or host environment variables
- **THEN** the server starts using default port 8080 and binds to localhost

#### Scenario: Configured port environment variable
- **WHEN** the server is executed with PORT environment variable set
- **THEN** the HTTP listener binds and listens on the specified port
