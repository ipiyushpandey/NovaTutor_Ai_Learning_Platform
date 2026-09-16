# Run and test NovaTutor

## Backend

From the `backend/` folder, configure the required environment variables from `backend/.env.example`, including a real `JWT_SECRET` and (for AI features) `GEMINI_API_KEY`. Then run:

```bash
mvn spring-boot:run
```

The backend uses MySQL. Override `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` for your local setup.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## AI smoke test

Open `http://localhost:8080/api/ai/status`. With a configured Gemini key, the endpoint should report that the provider is configured.

Then open AI Teacher and try a simple question such as:

```text
Explain Java inheritance in simple terms.
```

## Production build

Backend CI runs Maven tests and packaging. Frontend CI installs dependencies and runs the Vite production build.
