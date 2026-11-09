# Local setup

[简体中文](local-development.md) · **English**

Run commands from the repository root. Use a dedicated demo environment, never an existing business database.

## Requirements

Recorded environment: Java 17, Maven 3.9, MySQL 8.4, Redis, Node 26.12.0 and npm 11.20.0. Node/npm versions are recorded in version files and package engines. Other versions require validation.

The launcher currently assumes macOS and `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`, plus Bash, lsof and curl. Review `scripts/start-backend.sh` before using another installation layout; cross-platform launcher compatibility is not claimed.

Default backend configuration connects to MySQL `localhost:3306/online_learn` as `root` and Redis `127.0.0.1:6379`. These addresses are in `learn_server-master/ape-admin/src/main/resources/application-dev.yml`. Commented `DB_HOST`, `DB_NAME` and similar entries in `.env.example` do not override those fixed addresses.

## 1. Create a disposable database

**The first SQL file drops and recreates tables.** If `online_learn` already exists, stop and prepare a separate demo MySQL instance. Do not ignore a failed database-creation command.

```sh
mysql -u root -p -e 'CREATE DATABASE `online_learn` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;'
```

Only after successful creation:

```sh
mysql -u root -p online_learn < sql/portfolio-demo.sql
mysql -u root -p online_learn < sql/migrate-ai-data-agent.sql
```

## 2. Configure local credentials

If `.env` is absent, create it; otherwise edit the existing file without overwriting it:

```sh
cp .env.example .env
chmod 600 .env
openssl rand -hex 32
```

Put the generated value in `JWT_SECRET` (at least 32 characters). Do not commit, screenshot or share it. Run the local configuration check:

```sh
bash scripts/start-backend.sh --prepare-env
```

The script prompts for the local MySQL root password and optional DeepSeek key without echoing input. Leave the AI key empty to run without generation. Configure your own `DRUID_PASSWORD` if enabling Druid monitoring. `.env` is sourced as trusted shell configuration; do not place untrusted commands in it. See [AI setup](ai-model-setup.en.md) for all supported variables and limits.

## 3. Install and start

```sh
npm ci --prefix learn_front-master
npm ci --prefix learn_front_mange-master
bash scripts/start-demo.sh
```

The launcher does not install dependencies or import SQL. MySQL and Redis must be running; ports 3000, 3001 and 8080 must be available. Backend startup uses `-DskipTests`; startup success is not test success.

- Student: `http://localhost:3001/login`
- Teacher/administrator: `http://localhost:3000/login`
- Backend: `http://127.0.0.1:8080`

Synthetic account passwords are not published; obtain demo access from the maintainer or configure credentials in your disposable environment. Starting services alone does not provide login credentials. Inspect `.run/backend.log`, `.run/student.log` and `.run/admin.log` for startup failures; redact secrets and personal paths before sharing.

```sh
bash scripts/stop-demo.sh
```

## Frontend configuration and checks

Both frontends use Vue 3 / Vite. Each frontend's ignored `.env.local` may set `VITE_API_BASE_URL`; the student's `VITE_ADMIN_PORT` controls management links. These are public settings, never places for secrets. Optional `VITE_DEMO_CONTENT=true` is limited to synthetic samples and defaults to false; see [localization](internationalization.en.md).

Run `npm test --prefix <frontend-directory>` and `npm run build --prefix <frontend-directory>`. Backend contracts use `mvn -f learn_server-master/pom.xml -pl ape-admin -am test` with local HTTP mocks, not paid models. Browser verification scripts require `ZHIXUE_DEMO_PASSWORD` supplied locally for the current isolated demo accounts. Do not commit it. See [verification notes](verification-notes.en.md) for evidence and limits.
