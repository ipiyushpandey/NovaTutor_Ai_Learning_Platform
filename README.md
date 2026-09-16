# NovaTutor

**NovaTutor** is a full-stack AI learning platform built with **Spring Boot, Java 21, MySQL, React and Vite**. It combines AI-assisted learning, structured courses, quizzes, progress tracking, GATE CSE preparation, coding practice and learning analytics in one application.

## Highlights

- AI Teacher and contextual doubt solving
- Course-based learning with structured 105-lesson curricula
- Adaptive quizzes, practice and progress tracking
- GATE CSE preparation module with subject-wise tests and notes
- Knowledge Hub and PDF-based learning workflows
- Coding workspace with guarded sandbox execution
- Planner, revision, flashcards, mentor, interviewer and voice-tutor features
- JWT authentication with role-based admin controls
- MySQL persistence using Spring Data JPA/Hibernate
- Environment-based configuration for database, JWT, Gemini and CORS settings
- Production Docker configuration and GitHub Actions CI

## Tech Stack

| Layer | Technology |
| --- | --- |
| Frontend | React 18, Vite |
| Backend | Spring Boot 3.5.x, Java 21 |
| Database | MySQL 8.x, JPA/Hibernate |
| AI | Google Gemini API |
| Security | Spring Security, JWT |
| Documents | Apache PDFBox |
| Deployment | Docker, Nginx |

## Project Structure

```text
NovaTutor/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── .env.example
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── .env.example
├── database/
├── deploy/
├── docs/
├── .github/workflows/
├── .gitignore
└── README.md
```

## Requirements

- Java 21
- Maven 3.9+
- Node.js 20+ and npm
- MySQL 8.x
- A Gemini API key for AI features

## Local Setup

### 1. Configure the backend

Copy `backend/.env.example` into your environment or IntelliJ run configuration and set real values for:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GEMINI_API_KEY`
- `CORS_ORIGINS`

Never commit a real `.env` file or production credentials.

### 2. Start MySQL

Create the MySQL database/user required by your environment configuration. The default development URL can create the `ai_tutor` database automatically when the configured MySQL account has permission to do so.

### 3. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The backend runs on `http://localhost:8080` by default.

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite development server normally runs on `http://localhost:5173`.

## Production

Production configuration is kept under `deploy/`. The example environment file contains placeholders only:

```bash
cp deploy/.env.production.example deploy/.env.production
```

Set every required value before starting the production stack. Do not commit the resulting environment file.

The production compose setup keeps code execution disabled by default. If the sandbox is enabled, it should be reviewed and tested separately before exposing it to untrusted users.

## Security Notes

- Secrets are loaded from environment variables rather than source code.
- JWT signing requires an explicitly configured secret.
- Admin credentials are supplied through environment configuration.
- Production CORS and database credentials are deployment-specific.
- Generated caches, local environment files and build artifacts are ignored by Git.

## Documentation

- `PRODUCTION_SETUP.md` — environment and deployment configuration
- `RUN_AND_TEST.md` — local run/test workflow
- `GATE_CSE_MODULE.md` — GATE CSE module notes
- `deploy/` — Docker and Nginx production configuration

## License

This repository contains the NovaTutor application source. Third-party learning resources included under `frontend/public/gate-cse/notes/` retain their respective licenses and attribution requirements.
