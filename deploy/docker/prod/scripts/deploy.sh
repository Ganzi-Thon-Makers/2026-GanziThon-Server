#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Ganzithon PROD Blue/Green 배포 오케스트레이터
#
# - self-hosted 러너(=prod 서버) 또는 서버 쉘에서 직접 실행
# - 시크릿은 환경변수로만 주입한다. (.env 파일에 쓰지 않음)
# - nginx는 컨테이너로 동작하며, upstream-app.conf 갱신 후
#   docker exec ... nginx -s reload 로 무중단 전환한다.
#
# 흐름:
#   활성 색 판별 → 이미지 빌드 → 반대 색 기동 → 헬스체크
#   → upstream 전환 + reload → HTTPS 검증 → 이전 색 종료
# 실패 시 nginx를 되돌리고(전환 전이면 손대지 않고) 알림 후 종료.
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 경로: 스크립트 위치 기준 (deploy/docker/prod/scripts → deploy/docker/prod → repo root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"
REPO_ROOT="$(cd "$DEPLOY_DIR/../../.." && pwd)"

BLUE_COMPOSE="$DEPLOY_DIR/docker-compose.blue.yaml"
GREEN_COMPOSE="$DEPLOY_DIR/docker-compose.green.yaml"
NGINX_COMPOSE="$DEPLOY_DIR/docker-compose.nginx.yaml"

BLUE_PROJECT="ganzithon-prod-blue"
GREEN_PROJECT="ganzithon-prod-green"
NGINX_PROJECT="ganzithon-prod"

NGINX_CONTAINER="ganzithon-prod-nginx"

# upstream 파일은 repo 밖에 둔다: repo 안 파일은 actions/checkout이
# 매 배포마다 리셋하여 blue/green 상태가 유실되고, nginx 재시작 시
# 내려간 색을 가리켜 장애가 날 수 있다.
# repo의 deploy/nginx/prod/upstream-app.conf는 최초 seed용 템플릿이다.
STATE_DIR="${GANZITHON_PROD_STATE_DIR:-$HOME/.ganzithon-prod}"
UPSTREAM_FILE="$STATE_DIR/upstream-app.conf"
UPSTREAM_TEMPLATE="$REPO_ROOT/deploy/nginx/prod/upstream-app.conf"

BLUE_CONTAINER="ganzithon-prod-app-blue"
GREEN_CONTAINER="ganzithon-prod-app-green"
BLUE_PORT=8081
GREEN_PORT=8082

NETWORK="ganzithon-prod-net"
IMAGE="ganzithon-server:prod"
DOMAIN="api.ganzithon.com"

MAX_RETRY=20
RETRY_INTERVAL=15

# Discord 알림용 선택 env (webhook/API 문서 URL). 앱 시크릿은 받지 않는다.
# repo 안 경로는 actions/checkout의 git clean -ffdx로 지워지므로 repo 밖에 둔다.
ENV_FILE="$STATE_DIR/.env"
if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

DISCORD_NOTIFY="$SCRIPT_DIR/discord-notify.sh"
if [ -f "$DISCORD_NOTIFY" ]; then
  # shellcheck disable=SC1090
  source "$DISCORD_NOTIFY"
fi

notify() {
  # notify <함수명> [args...] — webhook 미설정이거나 알림 실패해도 배포 흐름은 유지
  if [ -n "${PROD_DISCORD_WEBHOOK_URL:-}" ] && [ -f "$DISCORD_NOTIFY" ]; then
    "$@" || echo -e "${YELLOW}[WARN] Discord 알림 실패 (배포 결과에는 영향 없음)${NC}"
  fi
}

dc() {
  # dc <project> <compose-file> <args...>
  docker compose -p "$1" -f "$2" "${@:3}"
}

echo -e "${YELLOW}[1/8] 필수 환경변수 검증${NC}"
REQUIRED_VARS=(
  PROD_DB_URL
  PROD_DB_USER
  PROD_DB_PASSWORD
  NCP_BUCKET
  NCP_ACCESS_KEY
  NCP_SECRET_KEY
  JWT_SECRET
)
for var in "${REQUIRED_VARS[@]}"; do
  if [ -z "${!var:-}" ]; then
    echo -e "${RED}[ERROR] ${var} is not configured${NC}" >&2
    exit 1
  fi
done
echo -e "${GREEN}[SUCCESS] 환경변수 확인 완료${NC}"

echo -e "${YELLOW}[2/8] 네트워크 / upstream 파일 / nginx 확인${NC}"

# 공유 네트워크 (최초 1회 자동 생성)
if ! docker network inspect "$NETWORK" > /dev/null 2>&1; then
  docker network create "$NETWORK" > /dev/null
  echo -e "${GREEN}[SUCCESS] 네트워크 생성: $NETWORK${NC}"
fi

# upstream 파일이 없으면 repo 템플릿으로 초기화
# (없는 상태로 nginx를 띄우면 Docker가 디렉토리를 만들어 mount가 깨진다)
mkdir -p "$STATE_DIR"
if [ ! -f "$UPSTREAM_FILE" ]; then
  if [ -f "$UPSTREAM_TEMPLATE" ]; then
    cp "$UPSTREAM_TEMPLATE" "$UPSTREAM_FILE"
  else
    cat > "$UPSTREAM_FILE" <<'EOF'
set $upstream http://ganzithon-prod-app-blue:8080;
EOF
  fi
  echo -e "${YELLOW}[INFO] upstream 파일 초기화: $UPSTREAM_FILE${NC}"
fi

# nginx 컨테이너 기동 (이미 실행 중이면 no-op)
dc "$NGINX_PROJECT" "$NGINX_COMPOSE" up -d

echo -e "${YELLOW}[3/8] 현재 active 색 확인${NC}"

# 기준은 "실행 중인 컨테이너"다. 파일은 checkout 리셋/수동 조작으로
# stale해질 수 있어 보조 수단으로만 사용한다.
BLUE_RUNNING=$(docker ps -q --filter "name=${BLUE_CONTAINER}" --filter "status=running")
GREEN_RUNNING=$(docker ps -q --filter "name=${GREEN_CONTAINER}" --filter "status=running")

if [ -n "$BLUE_RUNNING" ] && [ -z "$GREEN_RUNNING" ]; then
  CURRENT_COLOR="blue"
elif [ -n "$GREEN_RUNNING" ] && [ -z "$BLUE_RUNNING" ]; then
  CURRENT_COLOR="green"
else
  # 둘 다 실행 중(중단된 배포 잔여)이거나 둘 다 없음(최초 배포) → 파일 참고
  CURRENT_COLOR=$(grep -oE 'app-(blue|green)' "$UPSTREAM_FILE" | cut -d- -f2 | head -n 1 || true)
  [ -n "$BLUE_RUNNING" ] && [ -n "$GREEN_RUNNING" ] && \
    echo -e "${YELLOW}[WARN] 두 색 모두 실행 중 - upstream 파일 기준으로 판별${NC}"
fi

if [ "$CURRENT_COLOR" = "blue" ]; then
  TARGET="green"
  TARGET_PORT=$GREEN_PORT
  TARGET_PROJECT=$GREEN_PROJECT
  TARGET_COMPOSE=$GREEN_COMPOSE
  TARGET_CONTAINER=$GREEN_CONTAINER

  OLD_PROJECT=$BLUE_PROJECT
  OLD_COMPOSE=$BLUE_COMPOSE
  OLD_PORT=$BLUE_PORT
elif [ "$CURRENT_COLOR" = "green" ]; then
  TARGET="blue"
  TARGET_PORT=$BLUE_PORT
  TARGET_PROJECT=$BLUE_PROJECT
  TARGET_COMPOSE=$BLUE_COMPOSE
  TARGET_CONTAINER=$BLUE_CONTAINER

  OLD_PROJECT=$GREEN_PROJECT
  OLD_COMPOSE=$GREEN_COMPOSE
  OLD_PORT=$GREEN_PORT
else
  # 최초 배포 또는 판별 실패 → blue로 시작
  TARGET="blue"
  TARGET_PORT=$BLUE_PORT
  TARGET_PROJECT=$BLUE_PROJECT
  TARGET_COMPOSE=$BLUE_COMPOSE
  TARGET_CONTAINER=$BLUE_CONTAINER

  OLD_PROJECT=""
  OLD_COMPOSE=""
  OLD_PORT=""
fi

echo -e "${GREEN}[INFO] 현재: ${CURRENT_COLOR:-none} → 배포: $TARGET ($TARGET_PORT)${NC}"

echo -e "${YELLOW}[4/8] 이미지 빌드${NC}"
dc "$TARGET_PROJECT" "$TARGET_COMPOSE" build app

NEW_IMAGE=$(docker image inspect -f '{{.Id}}' "$IMAGE" 2>/dev/null || true)
echo -e "${GREEN}[SUCCESS] 새 이미지: ${NEW_IMAGE:-unknown}${NC}"

echo -e "${YELLOW}[5/8] $TARGET 컨테이너 실행 및 헬스체크${NC}"

dc "$TARGET_PROJECT" "$TARGET_COMPOSE" up -d --no-build

RETRY_COUNT=0
HEALTH_OK=false

while [ $RETRY_COUNT -lt $MAX_RETRY ]; do
  if curl -f -s --max-time 5 "http://localhost:${TARGET_PORT}/actuator/health" > /dev/null; then
    HEALTH_OK=true
    break
  fi
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo -e "${YELLOW}[RETRY] ${RETRY_COUNT}/${MAX_RETRY}${NC}"
  sleep $RETRY_INTERVAL
done

if [ "$HEALTH_OK" = false ]; then
  echo -e "${RED}[ERROR] 헬스체크 실패 - nginx 전환 없이 롤백 (기존 서버 무중단 유지)${NC}"
  docker logs --tail 100 "$TARGET_CONTAINER" >&2 || true
  dc "$TARGET_PROJECT" "$TARGET_COMPOSE" down || true
  notify notify_rollback_success
  exit 1
fi
echo -e "${GREEN}[SUCCESS] 헬스체크 통과${NC}"

echo -e "${YELLOW}[6/8] nginx upstream 전환 ($TARGET)${NC}"

OLD_UPSTREAM_CONTENT=$(cat "$UPSTREAM_FILE")

# 단일 파일 bind mount는 inode 교체(mv)가 통하지 않으므로 제자리 덮어쓰기
cat > "$UPSTREAM_FILE" <<EOF
set \$upstream http://${TARGET_CONTAINER}:8080;
EOF

if ! docker exec "$NGINX_CONTAINER" nginx -t; then
  echo -e "${RED}[ERROR] nginx 설정 검증 실패 - upstream 원복${NC}" >&2
  printf '%s\n' "$OLD_UPSTREAM_CONTENT" > "$UPSTREAM_FILE"
  dc "$TARGET_PROJECT" "$TARGET_COMPOSE" down || true
  notify notify_rollback_failed
  exit 1
fi

docker exec "$NGINX_CONTAINER" nginx -s reload
echo -e "${GREEN}[SUCCESS] nginx 전환 완료${NC}"

echo -e "${YELLOW}[7/8] 전환 후 HTTPS 검증${NC}"

SWITCH_OK=false
for _ in $(seq 1 10); do
  if curl -f -s --max-time 5 \
    --resolve "${DOMAIN}:443:127.0.0.1" \
    "https://${DOMAIN}/actuator/health" > /dev/null; then
    SWITCH_OK=true
    break
  fi
  sleep 3
done

if [ "$SWITCH_OK" = false ]; then
  echo -e "${RED}[ERROR] 전환 후 검증 실패 - 이전 색으로 자동 롤백${NC}" >&2
  printf '%s\n' "$OLD_UPSTREAM_CONTENT" > "$UPSTREAM_FILE"
  docker exec "$NGINX_CONTAINER" nginx -t && docker exec "$NGINX_CONTAINER" nginx -s reload || true

  # 이전 색이 아직 서빙 가능한지 확인
  OLD_OK=false
  if [ -n "$OLD_PORT" ] && curl -f -s --max-time 5 "http://localhost:${OLD_PORT}/actuator/health" > /dev/null; then
    OLD_OK=true
    dc "$TARGET_PROJECT" "$TARGET_COMPOSE" down || true
  fi

  if [ "$OLD_OK" = true ]; then
    notify notify_rollback_success
  else
    # 이전 색도 불건전 → 수동 개입 필요. 타겟은 남겨둔다.
    notify notify_rollback_failed
  fi
  exit 1
fi
echo -e "${GREEN}[SUCCESS] HTTPS 검증 통과${NC}"

echo -e "${YELLOW}[8/8] 이전 컨테이너 종료 및 정리${NC}"

if [ -n "$OLD_PROJECT" ]; then
  dc "$OLD_PROJECT" "$OLD_COMPOSE" down || true
fi

docker image prune -f || true
docker builder prune -f --filter "until=72h" > /dev/null || true

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}[DEPLOY SUCCESS] $TARGET 전환 완료${NC}"
echo -e "${GREEN}============================================${NC}"

notify notify_deploy_success "${NEW_IMAGE:-unknown}" "${PROD_API_DOCS_URL:-}" "${COMMIT_MESSAGE:-}" "${DEPLOYER:-}"
