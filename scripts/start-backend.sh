#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
ENV_EXAMPLE_FILE="$ROOT_DIR/.env.example"
SERVER_DIR="$ROOT_DIR/learn_server-master"
JAR_FILE="$SERVER_DIR/ape-admin/target/ape-admin-0.0.1-SNAPSHOT.jar"
JAVA17_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"

ensure_env_file() {
  if [[ -f "$ENV_FILE" ]]; then
    return
  fi

  if [[ -f "$ENV_EXAMPLE_FILE" ]]; then
    cp "$ENV_EXAMPLE_FILE" "$ENV_FILE"
  else
    touch "$ENV_FILE"
  fi
  chmod 600 "$ENV_FILE" 2>/dev/null || true
}

load_env_file() {
  set -a
  source "$ENV_FILE"
  set +a
}

env_has_key() {
  local key="$1"
  grep -qE "^${key}=" "$ENV_FILE"
}

shell_quote() {
  local value="$1"
  printf "%q" "$value"
}

write_env_value() {
  local key="$1" value="$2" tmp_file
  tmp_file="$(mktemp "${ENV_FILE}.tmp.XXXXXX")"
  if [[ -f "$ENV_FILE" ]]; then
    grep -vE "^${key}=" "$ENV_FILE" >"$tmp_file" || true
  fi
  printf "%s=%s\n" "$key" "$(shell_quote "$value")" >>"$tmp_file"
  mv "$tmp_file" "$ENV_FILE"
  chmod 600 "$ENV_FILE" 2>/dev/null || true
}

ensure_db_password() {
  while [[ -z "${DB_PASSWORD:-}" || "${DB_PASSWORD:-}" == "your_password_here" ]]; do
    read -r -s -p "请输入本机 MySQL root 密码 (DB_PASSWORD): " DB_PASSWORD
    echo ""
    if [[ -z "$DB_PASSWORD" ]]; then
      echo "DB_PASSWORD 不能为空，请重新输入。"
    fi
  done
  write_env_value DB_PASSWORD "$DB_PASSWORD"
  export DB_PASSWORD
}

ensure_deepseek_key() {
  if env_has_key DEEPSEEK_API_KEY && [[ -n "${DEEPSEEK_API_KEY:-}" ]]; then
    return
  fi

  read -r -s -p "请输入 DeepSeek API Key (可直接回车跳过): " DEEPSEEK_API_KEY
  echo ""
  write_env_value DEEPSEEK_API_KEY "${DEEPSEEK_API_KEY:-}"
  export DEEPSEEK_API_KEY

  if [[ -z "${DEEPSEEK_API_KEY:-}" ]]; then
    echo "未设置 DEEPSEEK_API_KEY，AI 功能会按未配置状态运行。"
  fi
}

ensure_jwt_secret() {
  local secret="${JWT_SECRET:-}"
  if [[ -z "$secret" || ${#secret} -lt 32 ]]; then
    echo "JWT_SECRET 缺失或长度不足 32 字符。"
    echo "请执行 'openssl rand -hex 32'，并将结果写入 $ENV_FILE 的 JWT_SECRET。"
    exit 1
  fi
}

prepare_env() {
  ensure_env_file
  load_env_file
  ensure_db_password
  ensure_deepseek_key
  load_env_file
  ensure_jwt_secret
}

prepare_env

if [[ "${1:-}" == "--prepare-env" ]]; then
  exit 0
fi

if [[ ! -f "$JAR_FILE" ]] || find "$SERVER_DIR" \( -path "*/src/main/*" -o -name "pom.xml" \) -type f -newer "$JAR_FILE" -print -quit | grep -q .; then
  echo "Building the backend jar from current sources..."
  (cd "$SERVER_DIR" && JAVA_HOME="$JAVA17_HOME" PATH="$JAVA17_HOME/bin:$PATH" mvn clean package -DskipTests)
fi

cd "$SERVER_DIR"
export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
exec java -jar "$JAR_FILE"
