#!/usr/bin/env bash
set -euo pipefail

# certbot(systemd timer/cron)이 실행할 때는 PATH가 짧을 수 있어 명시
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

# 사용: certbot-deploy-hook.sh [nginx-컨테이너명]  (기본 dev)
# prod 갱신 hook: certbot renew --deploy-hook "<이 스크립트> ganzithon-prod-nginx"
CONTAINER="${1:-${GANZITHON_NGINX_CONTAINER:-ganzithon-dev-nginx}}"

if docker ps --format '{{.Names}}' | grep -qx "${CONTAINER}"; then
  docker exec "${CONTAINER}" nginx -t
  docker exec "${CONTAINER}" nginx -s reload
  echo "nginx reloaded after certificate renewal"
else
  echo "WARN: ${CONTAINER} is not running; skip reload" >&2
fi
