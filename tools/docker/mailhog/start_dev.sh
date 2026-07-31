#!/bin/bash
# Start MailHog for local dev. Idempotent — will bring the container up if it's not already running.
cd "$(dirname "$0")"
docker compose -f docker-compose.yml up -d
echo "MailHog is up:"
echo "  SMTP  : localhost:1025 (auth-server's spring.mail target)"
echo "  UI    : http://localhost:8025"
