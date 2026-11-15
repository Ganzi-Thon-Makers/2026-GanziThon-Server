#!/usr/bin/env bash
# NCP DEV 서버에서 실행되는 배포 스크립트.
set -euo pipefail

COMPOSE_FILE="deploy/docker/dev/docker-compose-dev.yml"
PROJECT="ganzithon-dev"
DOMAIN="dev-api.ganzithon.com"

REQUIRED_VARS=(
  DEV_DB_URL
  DEV_DB_USER
  DEV_DB_PASSWORD
  NCP_BUCKET
  NCP_ACCESS_KEY
  NCP_SECRET_KEY
)

for var in "${REQUIRED_VARS[@]}"; do
  if [ -z "${!var:-}" ]; then
    echo "ERROR: environment variable ${var} is not set (check GitHub Actions secrets)" >&2
    exit 1
  fi
  export "${var}"
done

cd "$(git rev-parse --show-toplevel)"

dc() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

# 같은 이름(container_name)인데 이 compose 프로젝트 소속이 아닌 컨테이너가 남아 있으면
# "container name already in use" 로 배포가 실패하므로 미리 제거
remove_foreign_container() {
  local name="$1"
  if docker inspect "${name}" >/dev/null 2>&1; then
    local owner
    owner="$(docker inspect --format '{{ index .Config.Labels "com.docker.compose.project" }}' "${name}" 2>/dev/null || true)"
    if [ "${owner}" != "${PROJECT}" ]; then
      echo "Removing leftover container ${name} (compose project: '${owner:-none}')"
      docker rm -f "${name}"
    fi
  fi
}

echo "[1/5] Validate nginx config"
if ! dc run --rm -T --no-deps nginx nginx -t; then
  echo "ERROR: nginx config test failed" >&2
  exit 1
fi

echo "[2/5] Build new app image (running containers stay up until the build succeeds)"
dc build app

echo "[3/5] Replace containers"
remove_foreign_container ganzithon-dev-app
remove_foreign_container ganzithon-dev-nginx
# 이전 컨테이너를 제거하고 새로 생성. git reset --hard 로 파일 inode가 바뀌어도 nginx 설정이 반영됨
dc up -d --force-recreate --remove-orphans

echo "[4/5] Wait for containers to be healthy"
for container in ganzithon-dev-app ganzithon-dev-nginx; do
  healthy=false
  status="missing"
  for _ in $(seq 1 60); do
    status="$(docker inspect --format '{{.State.Health.Status}}' "${container}" 2>/dev/null || echo missing)"
    if [ "${status}" = "healthy" ]; then
      healthy=true
      break
    fi
    sleep 5
  done
  if [ "${healthy}" != true ]; then
    echo "ERROR: ${container} is not healthy (status: ${status})" >&2
    docker logs --tail 100 "${container}" >&2 || true
    exit 1
  fi
  echo "${container}: healthy"
done

echo "[5/5] Verify through nginx (HTTPS)"
for path in /actuator/health /v3/api-docs/service /v3/api-docs/backoffice /swagger-ui/index.html; do
  if ! curl -fsS -o /dev/null --resolve "${DOMAIN}:443:127.0.0.1" "https://${DOMAIN}${path}"; then
    echo "ERROR: https://${DOMAIN}${path} failed" >&2
    docker logs --tail 100 ganzithon-dev-app >&2 || true
    exit 1
  fi
  echo "OK ${path}"
done

echo "Cleanup old resources"
# 이 프로젝트의 중지된 컨테이너, 태그를 잃은 이전 앱 이미지(dangling), 3일 넘은 빌드 캐시 제거
docker container prune -f --filter "label=com.docker.compose.project=${PROJECT}" >/dev/null
docker image prune -f
docker builder prune -f --filter "until=72h" >/dev/null

echo "Deploy OK: https://${DOMAIN}/swagger-ui/index.html"
