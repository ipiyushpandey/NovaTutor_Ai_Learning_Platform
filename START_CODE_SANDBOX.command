#!/bin/bash
set -e
if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI not found. Install Docker Desktop first."
  exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "Docker Desktop is not running. Start it and run this file again."
  exit 1
fi
for image in eclipse-temurin:21-jdk python:3.12-alpine gcc:14 node:22-alpine golang:1.24-alpine; do
  echo "Preparing $image..."
  docker pull "$image"
done
echo "NovaTutor sandbox images are ready. Start/restart Spring Boot with CODE_RUNNER_ENABLED=true."
