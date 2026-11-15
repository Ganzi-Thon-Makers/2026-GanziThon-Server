#!/usr/bin/env bash
set -euo pipefail

# certbot(systemd timer/cron)이 실행할 때는 PATH가 짧을 수 있어 명시
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

CONTAINER="ganzithon-dev-nginx"

if docker ps --format '{{.Names}}' | grep -qx "${CONTAINER}"; then
  docker exec "${CONTAINER}" nginx -t
  docker exec "${CONTAINER}" nginx -s reload
  echo "nginx reloaded after certificate renewal"
else
  echo "WARN: ${CONTAINER} is not running; skip reload" >&2
fi
