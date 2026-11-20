#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Ganzithon PROD 시스템 모니터 (cron 주기 실행)
#
# - nginx upstream 파일에서 활성 색(blue/green)을 읽어 해당
#   컨테이너/포트를 감시한다. 읽기 실패 시 blue로 fallback.
# - 헬스체크 3회 연속 실패해야 down 판정 → @here 알림.
#   복구되면 다운타임(분)을 포함해 복구 알림.
# - 디스크 90%(경고)/95%(심각), 메모리 90%, 활성 컨테이너 중지 감시.
# - 상태 파일로 중복 알림 방지 (경고 1회, 정상 복귀 시 리셋).
#
# cron 예시:
#   */1 * * * * /path/to/makers-server/deploy/docker/prod/scripts/system-monitor.sh >> /var/log/ganzithon-monitor.log 2>&1
#
# 사전 조건: jq, curl, docker
# webhook: deploy/docker/prod/.env 의 PROD_DISCORD_WEBHOOK_URL
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"
REPO_ROOT="$(cd "$DEPLOY_DIR/../../.." && pwd)"

# 상태 파일은 repo checkout이 clean되며 날아가지 않도록 repo 밖에 둔다.
STATUS_FILE="${GANZITHON_MONITOR_STATUS_FILE:-$HOME/.ganzithon-prod/system-monitor.status}"

DISK_THRESHOLD=90
DISK_CRITICAL_THRESHOLD=95
MEMORY_THRESHOLD=90
MAX_HEALTH_FAILURES=3

# upstream 파일은 deploy.sh와 같은 repo 밖 경로를 본다
# (repo 안 파일은 actions/checkout 리셋으로 실제 active 색과 어긋남)
STATE_DIR="${GANZITHON_PROD_STATE_DIR:-$HOME/.ganzithon-prod}"
UPSTREAM_FILE="$STATE_DIR/upstream-app.conf"

# webhook은 .env에서만 읽는다 (앱 시크릿은 이 파일에 두지 않는다)
# repo 안 경로는 actions/checkout의 git clean -ffdx로 지워지므로 repo 밖에 둔다.
ENV_FILE="$STATE_DIR/.env"
if [ -f "$ENV_FILE" ]; then
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
fi

# 모니터링 전용 채널 분리: PROD_MONITOR_DISCORD_WEBHOOK_URL이 있으면
# 모니터 알림만 그 채널로 보낸다. 없으면 배포 알림과 같은 채널 fallback.
export PROD_DISCORD_WEBHOOK_URL="${PROD_MONITOR_DISCORD_WEBHOOK_URL:-${PROD_DISCORD_WEBHOOK_URL:-}}"

DISCORD_NOTIFY="$SCRIPT_DIR/discord-notify.sh"
if [ -f "$DISCORD_NOTIFY" ]; then
    # shellcheck disable=SC1090
    source "$DISCORD_NOTIFY"
fi

# 이전 상태 로드 (기본값 먼저 선언 후 파일로 덮어쓰기)
HEALTH_STATUS="up"
HEALTH_FAILURE_COUNT=0
HEALTH_DOWN_SINCE=""
DISK_WARNING_SENT=false
DISK_CRITICAL_SENT=false
MEMORY_WARNING_SENT=false
CONTAINER_WARNING_SENT=false

if [ -f "$STATUS_FILE" ]; then
    # shellcheck disable=SC1090
    source "$STATUS_FILE"
fi

DISK_CRITICAL_SENT="${DISK_CRITICAL_SENT:-false}"
DISK_WARNING_SENT="${DISK_WARNING_SENT:-false}"
MEMORY_WARNING_SENT="${MEMORY_WARNING_SENT:-false}"
CONTAINER_WARNING_SENT="${CONTAINER_WARNING_SENT:-false}"
HEALTH_FAILURE_COUNT="${HEALTH_FAILURE_COUNT:-0}"
HEALTH_STATUS="${HEALTH_STATUS:-up}"

# 감시 대상 결정: 실행 중인 컨테이너가 기준, 모호하면 upstream 파일 참고
BLUE_RUNNING=$(docker ps -q --filter "name=ganzithon-prod-app-blue" --filter "status=running" || true)
GREEN_RUNNING=$(docker ps -q --filter "name=ganzithon-prod-app-green" --filter "status=running" || true)

if [ -n "$BLUE_RUNNING" ] && [ -z "$GREEN_RUNNING" ]; then
    ACTIVE_COLOR="blue"
elif [ -n "$GREEN_RUNNING" ] && [ -z "$BLUE_RUNNING" ]; then
    ACTIVE_COLOR="green"
elif [ -f "$UPSTREAM_FILE" ]; then
    ACTIVE_COLOR=$(grep -oE 'app-(blue|green)' "$UPSTREAM_FILE" | cut -d- -f2 | head -n 1 || true)
else
    ACTIVE_COLOR=""
fi

case "$ACTIVE_COLOR" in
    blue)
        HEALTH_CHECK_URL="http://localhost:8081/actuator/health"
        CONTAINER_NAME="ganzithon-prod-app-blue"
        ;;
    green)
        HEALTH_CHECK_URL="http://localhost:8082/actuator/health"
        CONTAINER_NAME="ganzithon-prod-app-green"
        ;;
    *)
        echo "[WARN] nginx upstream에서 active 색을 읽지 못했습니다. blue(8081)로 fallback합니다."
        HEALTH_CHECK_URL="http://localhost:8081/actuator/health"
        CONTAINER_NAME="ganzithon-prod-app-blue"
        ;;
esac

# --- 헬스체크 ---
if curl -f -s --max-time 5 "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
    if [ "$HEALTH_STATUS" = "down" ]; then
        if [ -n "$HEALTH_DOWN_SINCE" ]; then
            DOWN_TIMESTAMP=$(date -d "$HEALTH_DOWN_SINCE" +%s 2>/dev/null || echo 0)
            UP_TIMESTAMP=$(date +%s)
            DOWN_DURATION=$(( UP_TIMESTAMP - DOWN_TIMESTAMP ))
            DOWNTIME_MIN=$(( DOWN_DURATION / 60 ))
            notify_server_up "${DOWNTIME_MIN}분" 2>/dev/null || true
        fi
        HEALTH_STATUS="up"
        HEALTH_FAILURE_COUNT=0
        HEALTH_DOWN_SINCE=""
    else
        HEALTH_FAILURE_COUNT=0
    fi
else
    HEALTH_FAILURE_COUNT=$((HEALTH_FAILURE_COUNT + 1))
    if [ "$HEALTH_FAILURE_COUNT" -ge "$MAX_HEALTH_FAILURES" ] && [ "$HEALTH_STATUS" = "up" ]; then
        HEALTH_STATUS="down"
        HEALTH_DOWN_SINCE=$(date '+%Y-%m-%d %H:%M:%S')
        notify_server_down 2>/dev/null || true
    fi
fi

# --- 디스크 ---
DISK_USAGE=$(df -h / | awk 'NR==2 {print $3 " / " $2}')
DISK_PERCENT=$(df / | awk 'NR==2 {gsub(/%/,"",$5); print int($5)}')
if [ "$DISK_PERCENT" -ge "$DISK_CRITICAL_THRESHOLD" ]; then
    if [ "$DISK_CRITICAL_SENT" = "false" ]; then
        notify_disk_critical "$DISK_USAGE" "$DISK_PERCENT" 2>/dev/null || true
        DISK_CRITICAL_SENT=true
    fi
else
    DISK_CRITICAL_SENT=false
fi
if [ "$DISK_PERCENT" -ge "$DISK_THRESHOLD" ]; then
    if [ "$DISK_WARNING_SENT" = "false" ]; then
        notify_disk_warning "$DISK_USAGE" "$DISK_PERCENT" 2>/dev/null || true
        DISK_WARNING_SENT=true
    fi
else
    DISK_WARNING_SENT=false
fi

# --- 메모리 ---
MEMORY_USAGE=$(free -h | awk 'NR==2 {print $3 " / " $2}')
MEMORY_PERCENT=$(free | awk 'NR==2 {print int($3/$2 * 100)}')
if [ "$MEMORY_PERCENT" -ge "$MEMORY_THRESHOLD" ]; then
    if [ "$MEMORY_WARNING_SENT" = "false" ]; then
        notify_memory_warning "$MEMORY_USAGE" "$MEMORY_PERCENT" 2>/dev/null || true
        MEMORY_WARNING_SENT=true
    fi
else
    MEMORY_WARNING_SENT=false
fi

# --- 활성 컨테이너 상태 ---
CONTAINER_RUNNING=$(docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" -q)
if [ -z "$CONTAINER_RUNNING" ]; then
    if [ "$CONTAINER_WARNING_SENT" = "false" ]; then
        notify_container_stopped 2>/dev/null || true
        CONTAINER_WARNING_SENT=true
    fi
else
    CONTAINER_WARNING_SENT=false
fi

# --- 상태 저장 (mktemp + mv 원자적 갱신) ---
mkdir -p "$(dirname "$STATUS_FILE")"
if TEMP_STATUS_FILE=$(mktemp "${STATUS_FILE}.tmp.XXXXXX" 2>/dev/null); then
    cat > "$TEMP_STATUS_FILE" << EOF
HEALTH_STATUS="$HEALTH_STATUS"
HEALTH_FAILURE_COUNT=$HEALTH_FAILURE_COUNT
HEALTH_DOWN_SINCE="$HEALTH_DOWN_SINCE"
DISK_WARNING_SENT=$DISK_WARNING_SENT
DISK_CRITICAL_SENT=$DISK_CRITICAL_SENT
MEMORY_WARNING_SENT=$MEMORY_WARNING_SENT
CONTAINER_WARNING_SENT=$CONTAINER_WARNING_SENT
EOF
    mv "$TEMP_STATUS_FILE" "$STATUS_FILE"
else
    echo "[WARN] STATUS_FILE 업데이트 실패 (디스크 공간 부족)"
fi
