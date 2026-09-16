# NovaTutor V16 — IntelliJ setup (Mac)

This package intentionally contains no `.idea` folder or stale Run Configuration. This prevents IntelliJ from retaining an old working-directory path.

## Backend
1. Open the **`backend`** folder as a Maven project, or open the NovaTutor root and import `backend/pom.xml`.
2. Open `backend/src/main/java/com/aitutor/AiTutorApplication.java`.
3. Right-click `AiTutorApplication.java` → **Run 'AiTutorApplication'**.
4. If creating a configuration manually:
   - Main class: `com.aitutor.AiTutorApplication`
   - Working directory: `$MODULE_WORKING_DIR$`
   - Module/classpath: the backend Maven module (`ai-tutor`)

## Frontend
```bash
cd frontend
npm install
npm run dev
```

## Important
Do not reuse an old IntelliJ Run Configuration whose working directory points to a previous NovaTutor folder name. Delete the old configuration and run `AiTutorApplication.java` directly once; IntelliJ will create a fresh configuration.
