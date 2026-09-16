# NovaTutor Production-Oriented Setup

## Backend environment

Set these values in the deployment environment. Use strong, unique secrets and never commit them to Git.

```text
DB_URL=jdbc:mysql://localhost:3306/ai_tutor?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Kolkata
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password
GEMINI_API_KEY=your_gemini_key
GEMINI_MODEL=gemini-3.6-flash
GEMINI_FALLBACK_MODEL=gemini-2.5-flash
JWT_SECRET=generate-a-long-random-secret-at-least-32-characters
ADMIN_EMAIL=your-admin-email@example.com
ADMIN_PASSWORD=generate-a-strong-admin-password
CORS_ORIGINS=http://localhost:5173
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

For deployment, set `VITE_API_URL` to the public backend base URL when the frontend and backend are hosted separately.

## Backend

Run `AiTutorApplication` from IntelliJ using Java 21, or use:

```bash
cd backend
mvn spring-boot:run
```

## Owner controls

The admin account is created from the `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables. No default admin password is stored in the repository.

## Security

Do not commit `.env` files, database passwords, JWT secrets, Gemini keys, API tokens or production credentials. The public repository contains placeholders only.
