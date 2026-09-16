#!/bin/bash
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
command -v mvn >/dev/null 2>&1 || { echo "Maven is not installed/in PATH. Start the backend from IntelliJ instead."; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "Node/npm is not installed/in PATH."; exit 1; }

cd "$ROOT/backend"
echo "Starting Spring Boot on http://localhost:8080 ..."
mvn spring-boot:run &
BACK_PID=$!
trap 'kill $BACK_PID 2>/dev/null || true' EXIT
sleep 6
cd "$ROOT/frontend"
npm install
echo "Starting Vite ..."
npm run dev
