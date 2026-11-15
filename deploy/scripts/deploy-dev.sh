#!/usr/bin/env bash
set -euo pipefail
# Configuration
COMPOSE_FILE="deploy/docker/dev/docker-compose-dev.yml"
PROJECT="ganzithon-dev"
DOMAIN="dev-api.ganzithon.com"

# Required Secrets
REQUIRED_VARS=(
  DEV_DB_URL
  DEV_DB_USER
  DEV_DB_PASSWORD
  NCP_BUCKET
  NCP_ACCESS_KEY
  NCP_SECRET_KEY
)

for var in "${REQUIRED_VARS[@]}";

do
  if [ -z "${!var:-}" ];
  then
    echo "ERROR: ${var} is not configured" >&2
    exit 1
  fi
  export "${var}"
done

cd "$(git rev-parse --show-toplevel)"
dc() {
  docker compose \
    -f "${COMPOSE_FILE}" \
    "$@"
}

echo "[1/5] Validate Nginx configuration"
if ! dc run --rm -T --no-deps nginx nginx -t;
then
  echo "ERROR: Nginx configuration validation failed" >&2
  exit 1
fi

echo "[2/5] Build Spring Boot image"
dc build app

echo "[3/5] Deploy containers"
dc up \
  -d \
  --force-recreate \
  --remove-orphans \
  --no-build

echo "[4/5] Waiting for healthy containers"
CONTAINERS=(
  ganzithon-dev-app
  ganzithon-dev-nginx
)

for container in "${CONTAINERS[@]}";

do
  healthy=false
  status="missing"
  for _ in $(seq 1 60);
  do
    status="$(
      docker inspect \
        --format '{{.State.Health.Status}}' \
        "${container}" 2>/dev/null || echo missing
    )"
    if [ "${status}" = "healthy" ];
    then
      healthy=true
      break
    fi
    sleep 5
  done

  if [ "${healthy}" != true ];
  then
    echo "ERROR: ${container} is not healthy" >&2
    echo "Status: ${status}" >&2
    docker logs --tail 100 "${container}" >&2 || true
    exit 1
  fi
  echo "${container}: healthy"
done


echo "[5/5] Verify HTTPS endpoints"
ENDPOINTS=(
  /actuator/health
  /v3/api-docs/platform
  /v3/api-docs/backoffice
  /swagger-ui/index.html
)

for path in "${ENDPOINTS[@]}";
 do
  if ! curl \
    --fail \
    --silent \
    --show-error \
    --output /dev/null \
    --resolve "${DOMAIN}:443:127.0.0.1" \
    "https://${DOMAIN}${path}";
    then
    echo "ERROR: HTTPS verification failed: ${path}" >&2
    docker logs --tail 100 ganzithon-dev-app >&2 || true
    exit 1
  fi
  echo "OK: ${path}"
done

echo "Cleaning up old Docker resources"
docker container prune \
  -f \
  --filter "label=com.docker.compose.project=${PROJECT}" \
  >/dev/null

docker image prune -f
docker builder prune \
  -f \
  --filter "until=72h" \
  >/dev/null

echo "=================================="
echo "DEV Deployment Completed"
echo "=================================="

echo "Swagger:"
echo "https://${DOMAIN}/swagger-ui/index.html"
