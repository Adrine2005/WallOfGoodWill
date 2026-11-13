# Docker & Cloud Deployment Guide: Wall of Goodwill Server

This guide walks you through building, running, and deploying the Go server Docker image locally and to free cloud hosting with **Supabase PostgreSQL & Storage**.

---

## 1. Quick Prerequisites

- **Docker Desktop** installed and running on your computer.
- A free **Supabase** account at [https://supabase.com](https://supabase.com).

---

## 2. Setting Up Your Free Supabase Backend (2 Minutes)

1. **Create a Supabase Project**:
   - Go to [database.new](https://database.new) and create a project (e.g., `goodwill-campus`).
   - Save your database password securely.
2. **Copy PostgreSQL Connection String**:
   - In Supabase, go to **Project Settings** ➔ **Database** ➔ **Connection String** (choose **URI** / **Transaction Pooler** or **Direct**).
   - Format:
     ```text
     postgres://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres
     ```
3. **Create Public Image Storage Bucket**:
   - In the left sidebar, click **Storage**.
   - Click **New Bucket**. Name it: `goodwill-images`.
   - Toggle **Public bucket** to `ON` (so students can view item photos without auth tokens).
   - Click **Save**.
4. **Copy API Keys**:
   - Go to **Project Settings** ➔ **API**.
   - Note down:
     - **Project URL** (e.g., `https://swdjtvousstllszjrxyo.supabase.co`)
     - **service_role secret key** (needed by the Go server to upload to storage).

---

## 3. Building the Docker Image Locally

Navigate to the `server/` directory and run:

```bash
cd server

# 1. Build the Docker image
docker build -t goodwill-server:latest .

# 2. Verify the built image size (it should be very lean, ~25MB)
docker images | grep goodwill-server
```

---

## 4. Running the Container Locally

### Option A: Run Container Connected to Supabase
Replace with your actual Supabase URL and password:

```bash
docker run -d \
  --name goodwill-server \
  -p 8080:8080 \
  -e PORT=8080 \
  -e DATABASE_URL="postgres://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres" \
  -e SUPABASE_URL="https://[PROJECT-REF].supabase.co" \
  -e SUPABASE_SERVICE_ROLE_KEY="[YOUR-SERVICE-ROLE-KEY]" \
  -e SUPABASE_STORAGE_BUCKET="goodwill-images" \
  goodwill-server:latest
```

### Option B: Run Offline Local Mode (In-Memory Fallback)
If you don't have internet or want to test quickly without Supabase:

```bash
docker run -d \
  --name goodwill-server \
  -p 8080:8080 \
  goodwill-server:latest
```

### Option C: Run Full Stack with Docker Compose (Local PostgreSQL)
If you want a local PostgreSQL database container running side-by-side:

```bash
docker compose up -d
```

---

## 5. Verifying the Running Server

Check logs:
```bash
docker logs -f goodwill-server
```
You should see:
```text
✓ Successfully connected to PostgreSQL database
✓ Successfully applied database migration: 001_initial_schema.sql
✓ Supabase Cloud Storage enabled for photo uploads
Wall of Goodwill server starting on port 8080 in production mode
```

Test health check:
```bash
curl http://localhost:8080/health
```
Response:
```json
{"status":"ok","timestamp":"2026-11-13T..."}
```

---

## 6. Deploying to Free 24/7 Cloud Hosting (Render.com)

Render allows you to host the Dockerized server 100% for free with HTTPS:

1. Push your repository to **GitHub**.
2. Sign in to [https://render.com](https://render.com) (free account).
3. Click **New +** ➔ **Web Service**.
4. Select your GitHub repository (`MAD`).
5. Choose **Docker** as runtime and set **Root Directory** to `server`.
6. Select the **Free** instance plan.
7. Under **Environment Variables**, add:
   - `DATABASE_URL`: `postgres://postgres:[PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres`
   - `SUPABASE_URL`: `https://[PROJECT-REF].supabase.co`
   - `SUPABASE_SERVICE_ROLE_KEY`: `[YOUR-SERVICE-ROLE-KEY]`
   - `SUPABASE_STORAGE_BUCKET`: `goodwill-images`
8. Click **Create Web Service**.
9. Render will automatically build the `Dockerfile` and give you a free live URL:
   `https://goodwill-server-xxxx.onrender.com`

---

## 7. Connecting the Android App to Cloud Server

In [`android/app/src/main/java/com/goodwill/app/data/remote/ApiClient.java`](file:///Users/adrinesolomon/MAD/android/app/src/main/java/com/goodwill/app/data/remote/ApiClient.java), you can point `BASE_URL` to your Render cloud URL:

```java
public static final String CLOUD_BASE_URL = "https://goodwill-server-xxxx.onrender.com/v1/";
```
Now your Android app works anywhere over WiFi/cellular without needing USB port forwarding or emulator loopbacks!
