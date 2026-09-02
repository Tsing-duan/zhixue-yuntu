#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
SERVER_DIR="$ROOT_DIR/learn_server-master"
STUDENT_DIR="$ROOT_DIR/learn_front-master"
ADMIN_DIR="$ROOT_DIR/learn_front_mange-master"
PROJECT_INSTANCE_ID="$(printf '%s' "$ROOT_DIR" | cksum | awk '{print $1}')"
SCREEN_PREFIX="zhixue-learning-$PROJECT_INSTANCE_ID"
OPEN_BROWSER=1

if [[ "${1:-}" == "--no-open" || "${ZHIXUE_NO_OPEN:-0}" == "1" || ! -t 1 ]]; then
  OPEN_BROWSER=0
fi

mkdir -p "$RUN_DIR"

if [[ -t 1 ]]; then
  BOLD="$(printf '\033[1m')"
  DIM="$(printf '\033[2m')"
  GREEN="$(printf '\033[32m')"
  YELLOW="$(printf '\033[33m')"
  RESET="$(printf '\033[0m')"
else
  BOLD=""
  DIM=""
  GREEN=""
  YELLOW=""
  RESET=""
fi

step() {
  printf '\n%b\n' "${BOLD}[$1/7] $2${RESET}"
}

detail() {
  printf '  %b\n' "${DIM}$*${RESET}"
}

ok() {
  printf '  %b\n' "${GREEN}[OK]${RESET} $*"
}

warn() {
  printf '  %b\n' "${YELLOW}[INFO]${RESET} $*"
}

port_pid() {
  lsof -tiTCP:"$1" -sTCP:LISTEN 2>/dev/null | head -n 1
}

assert_port_available_or_owned() {
  local label="$1" port="$2" directory="$3"
  local pid command cwd
  pid="$(port_pid "$port" || true)"
  [[ -z "$pid" ]] && return

  command="$(ps -p "$pid" -o command= 2>/dev/null || true)"
  cwd="$(lsof -nP -p "$pid" 2>/dev/null | awk '$4=="cwd" {print $NF; exit}')"
  if [[ "$command" != *"$directory"* && "$cwd" != "$directory" ]]; then
    echo "Port $port is occupied by another project; $label was not started and no external process was stopped."
    exit 1
  fi
}

can_use_detached_screen() {
  [[ "$(uname -s)" == "Darwin" ]] && command -v screen >/dev/null 2>&1
}

stop_screen_session() {
  local name="$1"
  screen -S "$name" -X quit >/dev/null 2>&1 || true
}

start_detached() {
  local name="$1" log_file="$2"
  shift 2

  stop_screen_session "$name"
  rm -f "$log_file"
  screen -dmS "$name" /bin/bash -c \
    'exec /usr/bin/env PATH="$1" "${@:3}" >>"$2" 2>&1' \
    _ "$PATH" "$log_file" "$@"
}

start_detached_in_directory() {
  local name="$1" log_file="$2" directory="$3"
  shift 3

  stop_screen_session "$name"
  rm -f "$log_file"
  screen -dmS "$name" /bin/bash -c \
    'cd "$1" && exec /usr/bin/env PATH="$2" "${@:4}" >>"$3" 2>&1' \
    _ "$directory" "$PATH" "$log_file" "$@"
}

ensure_paths() {
  local missing=0
  for path in "$SERVER_DIR" "$STUDENT_DIR" "$ADMIN_DIR" "$ROOT_DIR/scripts/start-backend.sh"; do
    if [[ ! -e "$path" ]]; then
      echo "Missing required path: $path"
      missing=1
    fi
  done
  if [[ "$missing" == "1" ]]; then
    exit 1
  fi
}

prepare_backend_port() {
  local pid
  pid="$(port_pid 8080 || true)"
  if [[ -n "$pid" ]]; then
    warn "Restarting the local backend to load current project code."
    kill "$pid"
    while [[ -n "$(port_pid 8080 || true)" ]]; do sleep 0.2; done
    ok "Backend port 8080 released."
  else
    ok "Backend port 8080 is free."
  fi
}

prepare_env() {
  "$ROOT_DIR/scripts/start-backend.sh" --prepare-env
}

start_backend() {
  if [[ -n "$(port_pid 8080 || true)" ]]; then
    ok "Backend already listening on http://127.0.0.1:8080."
    return
  fi
  rm -f "$RUN_DIR/backend.pid"
  if can_use_detached_screen; then
    start_detached "$SCREEN_PREFIX-backend" "$RUN_DIR/backend.log" \
      "$ROOT_DIR/scripts/start-backend.sh"
  else
    nohup "$ROOT_DIR/scripts/start-backend.sh" </dev/null >"$RUN_DIR/backend.log" 2>&1 &
    local pid="$!"
    echo "$pid" >"$RUN_DIR/backend.pid"
    disown "$pid" 2>/dev/null || true
  fi
  ok "Backend process started. Log: $RUN_DIR/backend.log"
}

start_frontend() {
  local label="$1" port="$2" directory="$3"
  local pid
  assert_port_available_or_owned "$label" "$port" "$directory"
  pid="$(port_pid "$port" || true)"
  if [[ -n "$pid" ]]; then
    ok "$label already listening on http://127.0.0.1:$port."
    return
  fi
  rm -f "$RUN_DIR/$label.pid"
  if can_use_detached_screen; then
    start_detached_in_directory "$SCREEN_PREFIX-$label" "$RUN_DIR/$label.log" "$directory" \
      "$(command -v npm)" run serve -- --no-open
  else
    (
      cd "$directory"
      nohup npm run serve -- --no-open </dev/null >"$RUN_DIR/$label.log" 2>&1 &
      local pid="$!"
      echo "$pid" >"$RUN_DIR/$label.pid"
      disown "$pid" 2>/dev/null || true
    )
  fi
  ok "$label process started. Log: $RUN_DIR/$label.log"
}

record_port_pid() {
  local label="$1" port="$2" pid
  pid="$(port_pid "$port" || true)"
  if [[ -n "$pid" ]]; then
    echo "$pid" >"$RUN_DIR/$label.pid"
  fi
}

wait_for_url() {
  local label="$1" url="$2"
  local attempt
  for attempt in {1..90}; do
    if curl -fsS "$url" >/dev/null 2>&1; then
      ok "$label ready: $url"
      return
    fi
    sleep 1
  done
  echo "$label did not become ready. See $RUN_DIR for logs."
  exit 1
}

step 1 "定位项目与启动脚本"
ensure_paths
ok "Project root: $ROOT_DIR"
detail "Runtime logs: $RUN_DIR"

step 2 "准备本机配置"
prepare_env
ok ".env ready. DB password is required; DeepSeek API Key may be empty."

step 3 "检查端口与旧进程"
assert_port_available_or_owned backend 8080 "$SERVER_DIR"
assert_port_available_or_owned student 3001 "$STUDENT_DIR"
assert_port_available_or_owned admin 3000 "$ADMIN_DIR"
prepare_backend_port

step 4 "启动 Spring Boot 后端"
start_backend

step 5 "启动学生端 Vue 服务"
start_frontend student 3001 "$STUDENT_DIR"

step 6 "启动管理端 Vue 服务"
start_frontend admin 3000 "$ADMIN_DIR"

step 7 "健康检查与打开入口"
wait_for_url backend "http://127.0.0.1:8080/"
wait_for_url student "http://127.0.0.1:3001/login"
wait_for_url admin "http://127.0.0.1:3000/"
record_port_pid backend 8080
record_port_pid student 3001
record_port_pid admin 3000

STUDENT_URL="http://localhost:3001/login"
ADMIN_URL="http://localhost:3000/"

if [[ "$OPEN_BROWSER" == "1" ]] && command -v open >/dev/null 2>&1; then
  open "$STUDENT_URL" "$ADMIN_URL"
  ok "Opened browser tabs."
fi

printf '\n'
printf '%s\n' "Student login: $STUDENT_URL"
printf '%s\n' "Admin / teacher login: $ADMIN_URL"
