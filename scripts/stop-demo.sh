#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
PROJECT_INSTANCE_ID="$(printf '%s' "$ROOT_DIR" | cksum | awk '{print $1}')"
SCREEN_PREFIX="zhixue-learning-$PROJECT_INSTANCE_ID"

port_pids() {
  lsof -tiTCP:"$1" -sTCP:LISTEN 2>/dev/null || true
}

process_owned_by_project() {
  local pid="$1" command cwd
  command="$(ps -p "$pid" -o command= 2>/dev/null || true)"
  cwd="$(lsof -nP -p "$pid" 2>/dev/null | awk '$4=="cwd" {print $NF; exit}')"
  [[ "$command" == *"$ROOT_DIR"* || "$cwd" == "$ROOT_DIR" || "$cwd" == "$ROOT_DIR/"* ]]
}

stop_pid_file() {
  local label="$1" file="$2"
  if [[ ! -f "$file" ]]; then
    return
  fi

  local pid
  pid="$(cat "$file" 2>/dev/null || true)"
  if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
    if process_owned_by_project "$pid"; then
      echo "Stopping $label process: $pid"
      kill "$pid" 2>/dev/null || true
    else
      echo "Skipping $label PID $pid because it belongs to another project."
    fi
  fi
  rm -f "$file"
}

stop_port() {
  local port="$1" pids pid stopped=0 skipped=0
  pids="$(port_pids "$port")"
  if [[ -z "$pids" ]]; then
    echo "No process listening on port $port"
    return
  fi

  while IFS= read -r pid; do
    [[ -z "$pid" ]] && continue
    if process_owned_by_project "$pid"; then
      echo "Stopping project process on port $port: $pid"
      kill "$pid" 2>/dev/null || true
      stopped=1
    else
      echo "Leaving external process on port $port untouched: $pid"
      skipped=1
    fi
  done <<<"$pids"

  if [[ "$stopped" == "0" && "$skipped" == "0" ]]; then
    echo "No process listening on port $port"
  fi
}

stop_screen_session() {
  local label="$1" name="$2"
  if command -v screen >/dev/null 2>&1 && screen -S "$name" -X quit >/dev/null 2>&1; then
    echo "Stopping $label detached session: $name"
  fi
}

stop_screen_session backend "$SCREEN_PREFIX-backend"
stop_screen_session student "$SCREEN_PREFIX-student"
stop_screen_session admin "$SCREEN_PREFIX-admin"

stop_pid_file backend "$RUN_DIR/backend.pid"
stop_pid_file student "$RUN_DIR/student.pid"
stop_pid_file admin "$RUN_DIR/admin.pid"

stop_port 8080
stop_port 3001
stop_port 3000

echo "All stop commands issued."
