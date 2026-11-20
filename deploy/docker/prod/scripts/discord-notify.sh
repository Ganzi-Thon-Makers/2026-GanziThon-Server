#!/usr/bin/env bash
# ============================================================
# Ganzithon PROD Discord 알림 공용 함수 (source해서 사용)
#
# 사전 조건: jq, curl 설치
# webhook: PROD_DISCORD_WEBHOOK_URL (없으면 DISCORD_WEBHOOK_URL fallback)
#   → deploy/docker/prod/.env 에 webhook만 상주시키면 된다.
# 주의: set -e를 두지 않는다. 호출 스크립트의 종료 정책을 침범하지 않기 위함.
#       알림 실패는 호출자를 죽이지 않도록 모든 curl에 || true를 단다.
# ============================================================

COLOR_RED=15158332
COLOR_GREEN=3066993
COLOR_BLUE=3447003
COLOR_YELLOW=16776960

DISCORD_WEBHOOK_URL="${DISCORD_WEBHOOK_URL:-}"
PROD_DISCORD_WEBHOOK_URL="${PROD_DISCORD_WEBHOOK_URL:-}"

# prod 전용이므로 단일 webhook으로 통일한다.
WEBHOOK_URL="${PROD_DISCORD_WEBHOOK_URL:-$DISCORD_WEBHOOK_URL}"

discord_post() {
    # discord_post <payload> — webhook 미설정/전송 실패 시 조용히 스킵
    [ -z "$WEBHOOK_URL" ] && return 0
    curl -s --max-time 10 -H "Content-Type: application/json" \
        -X POST -d "$1" "$WEBHOOK_URL" > /dev/null || true
}

notify_deploy_success() {
    local image_id=$1
    local api_docs_url=${2:-""}
    local commit_message=${3:-""}
    local deployer=${4:-""}
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg image_id "$image_id" \
      --arg msg "$commit_message" \
      --arg url "$api_docs_url" \
      --arg ts "$timestamp" \
      --arg deployer "$deployer" \
      --argjson color "$COLOR_GREEN" \
      '{
        username: "GanziThon Bot",
        embeds: [{
          title: "배포 완료 [PROD]",
          description: ("\($msg)\n\n[API 문서 바로가기](\($url))"),
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "이미지 ID", value: $image_id, inline: false},
            {name: "시간", value: $ts, inline: true},
            {name: "담당자", value: $deployer, inline: true},
            {name: "상태", value: "헬스체크 통과", inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_rollback_success() {
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_GREEN" \
      '{
        username: "GanziThon Bot",
        embeds: [{
          title: "배포 실패 - 자동 롤백 완료 [PROD]",
          description: "새 버전 기동 실패로 이전 버전을 계속 서빙합니다. (무중단 유지)",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "상태", value: "이전 버전 정상 서빙 중", inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_rollback_failed() {
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_RED" \
      '{
        username: "GanziThon Bot",
        content: "@here 롤백 실패",
        embeds: [{
          title: "롤백 실패 [PROD]",
          description: "롤백 후에도 서버가 정상 응답하지 않습니다. 수동 복구가 필요합니다.",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "상태", value: "수동 복구 필요", inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_server_down() {
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_RED" \
      '{
        username: "GanziThon Bot",
        content: "@here 서버 다운",
        embeds: [{
          title: "서버 다운 [PROD]",
          description: "서버가 응답하지 않습니다.",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "상태", value: "헬스체크 실패", inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_server_up() {
    local downtime=${1:-"알 수 없음"}
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg ts "$timestamp" \
      --arg dt "$downtime" \
      --argjson color "$COLOR_GREEN" \
      '{
        username: "GanziThon Bot",
        embeds: [{
          title: "서버 복구 [PROD]",
          description: "서버가 복구되었습니다.",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "복구 시간", value: $ts, inline: true},
            {name: "다운타임", value: $dt, inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_disk_critical() {
    local disk_usage=$1
    local disk_percent=$2
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg du "$disk_usage" \
      --arg dp "$disk_percent" \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_RED" \
      '{
        username: "GanziThon Bot",
        content: "@here 디스크 공간 부족",
        embeds: [{
          title: "디스크 사용량 심각 [PROD]",
          description: "디스크 사용량이 95%를 초과했습니다.",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "디스크 사용량", value: "\($du) (\($dp)%)", inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_disk_warning() {
    local disk_usage=$1
    local disk_percent=$2
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg du "$disk_usage" \
      --arg dp "$disk_percent" \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_YELLOW" \
      '{
        username: "GanziThon Bot",
        embeds: [{
          title: "디스크 사용량 경고 [PROD]",
          description: "디스크 사용량이 90%를 초과했습니다.",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "디스크 사용량", value: "\($du) (\($dp)%)", inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_memory_warning() {
    local memory_usage=$1
    local memory_percent=$2
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg mu "$memory_usage" \
      --arg mp "$memory_percent" \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_RED" \
      '{
        username: "GanziThon Bot",
        content: "@here 메모리 부족",
        embeds: [{
          title: "메모리 사용량 경고 [PROD]",
          description: "메모리 사용량이 임계값을 초과했습니다.",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "메모리 사용량", value: "\($mu) (\($mp)%)", inline: false}
          ]
        }]
      }')

    discord_post "$payload"
}

notify_container_stopped() {
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$WEBHOOK_URL" ] && return 0

    local payload
    payload=$(jq -n \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_RED" \
      '{
        username: "GanziThon Bot",
        content: "@here 컨테이너 중지",
        embeds: [{
          title: "컨테이너 중지 [PROD]",
          description: "활성 앱 컨테이너가 중지되었습니다.",
          color: $color,
          fields: [
            {name: "환경", value: "prod", inline: true},
            {name: "시간", value: $ts, inline: true}
          ]
        }]
      }')

    discord_post "$payload"
}

