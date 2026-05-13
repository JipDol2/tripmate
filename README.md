# TripMate MVP

TripMate is a travel companion matching MVP. Users can register, create companion posts, apply to join trips, review incoming applications, and manage basic profile and safety actions such as reporting or blocking other users.

## Tech Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Redis

### Frontend
- React 18
- Vite 5
- React Router
- Axios

## Main Features

- User registration and login
- Profile lookup and update
- Companion post list and detail view
- Companion post creation
- Trip application submission
- Sent application history
- Received application history for a post
- Application approval and rejection
- User reporting
- User blocking

## Project Structure

```text
tripmate-mvp/
  backend/    Spring Boot API server
  frontend/   React + Vite web client
```

## Prerequisites

Install the following before running the project locally:

- Java 17
- Node.js 18 or newer
- npm
- Docker Desktop

You can verify your local tools with:

```bash
java -version
node -v
npm -v
docker -v
docker compose version
```

## Local Development Setup

### 1. Start PostgreSQL and Redis

For full local development, use the compose file inside `backend/`. It starts both PostgreSQL and Redis, which the backend uses for token-related Redis operations.

From the repository root:

```bash
docker compose -f backend/docker-compose.yml up -d
```

Check that both containers are running:

```bash
docker ps
```

Expected services:

- `tripmate-postgres`
- `tripmate-redis`

To stop them later:

```bash
docker compose -f backend/docker-compose.yml down
```

To stop them and remove volumes:

```bash
docker compose -f backend/docker-compose.yml down -v
```

Note:
The root-level `docker-compose.yml` only starts PostgreSQL. It is not the recommended option for full local development because the backend also uses Redis.

### 2. Run the Backend

Move into the backend directory:

```bash
cd backend
```

Run the Spring Boot application with the Gradle wrapper.

macOS / Linux:

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

Default backend URL:

```text
http://localhost:8080
```

Base API path:

```text
http://localhost:8080/api
```

Important backend defaults from `backend/src/main/resources/application.yml`:

- `SERVER_PORT=8080`
- `DB_URL=jdbc:p6spy:postgresql://localhost:5432/tripmate`
- `DB_USERNAME=tripmate`
- `DB_PASSWORD=tripmate`
- `REDIS_HOST=localhost`
- `REDIS_PORT=6379`
- `JWT_SECRET=CHANGE_ME_CHANGE_ME_CHANGE_ME_CHANGE_ME_1234567890`
- `JWT_EXPIRATION_MS=86400000`

You can override them when starting the backend.

Windows PowerShell example:

```powershell
$env:DB_URL="jdbc:p6spy:postgresql://localhost:5432/tripmate"
$env:DB_USERNAME="tripmate"
$env:DB_PASSWORD="tripmate"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:JWT_SECRET="replace-with-a-long-random-secret"
.\gradlew.bat bootRun
```

macOS / Linux example:

```bash
DB_URL=jdbc:p6spy:postgresql://localhost:5432/tripmate \
DB_USERNAME=tripmate \
DB_PASSWORD=tripmate \
REDIS_HOST=localhost \
REDIS_PORT=6379 \
JWT_SECRET=replace-with-a-long-random-secret \
./gradlew bootRun
```

### 3. Run the Frontend

Open a second terminal, then move into the frontend directory:

```bash
cd frontend
```

Install packages:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

The frontend is currently configured to call:

```text
http://localhost:8080/api
```

That base URL is hardcoded in `frontend/src/lib/api.js`. If you run the backend on a different host or port, update that file accordingly.

## Recommended Run Order

1. Start PostgreSQL and Redis with Docker Compose.
2. Start the backend on port `8080`.
3. Start the frontend on port `5173`.
4. Open `http://localhost:5173` in your browser.

If the frontend loads but API requests fail, verify that the backend is running on `http://localhost:8080` and that Docker containers for PostgreSQL and Redis are healthy.

## First Test Account

You can create a test account from the UI, or call the registration API directly:

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "test@test.com",
  "password": "1234",
  "nickname": "TestUser",
  "ageRange": "30s",
  "gender": "MALE"
}
```

Full local URL:

```text
http://localhost:8080/api/auth/register
```

## Troubleshooting

### Port 5432, 6379, 8080, or 5173 is already in use

Stop the process using that port, or change the relevant port mapping and application setting before restarting the service.

### Backend cannot connect to PostgreSQL

Check the container status:

```bash
docker ps
```

Then inspect logs:

```bash
docker logs tripmate-postgres
```

### Backend cannot connect to Redis

Inspect the Redis container logs:

```bash
docker logs tripmate-redis
```

### CORS errors in the browser

The backend currently allows these frontend origins:

- `http://localhost:5173`
- `http://localhost:4173`
- `http://127.0.0.1:5173`

If you use a different frontend origin, update the CORS configuration in `backend/src/main/java/com/tripmate/config/SecurityConfig.java`.

## Future Improvements

- Refresh token flow
- Kakao or Google social login
- Image upload with S3
- Real-time chat
- Push notifications
- Review and trust scoring
- Identity verification
- Admin moderation screen
- Pagination and advanced filtering
- React Query integration
- Mobile packaging with Capacitor
