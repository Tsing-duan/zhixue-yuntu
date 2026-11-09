# AI model setup and operating boundaries

[简体中文](ai-model-setup.md) · **English** · [Project overview](../README.en.md)

This guide describes the repository implementation. Six generation endpoints share `DeepSeekClient`. It is **not a generic adapter for arbitrary OpenAI-compatible services**. The code permits official `https://api.deepseek.com`, model `deepseek-v4-flash`, `reasoning_effort=high`, `/chat/completions`, non-streaming output and thinking enabled. Do not append `/v1`. Changing providers, models or protocols requires adapter and contract-test changes.

The model name is a code constraint, not a promise about vendor availability or pricing. Current work uses local mocks and unconfigured branches; it has not called a real provider.

## Setup

1. Prepare the dedicated demo database, Redis, Java and frontends described in [Local demo](local-development.en.md). AI uses the existing authenticated platform session and Redis.
2. Only if `.env` is absent, run `umask 077; cp .env.example .env`. Otherwise edit the existing file. Configure database access and a `JWT_SECRET` at least 32 characters long. `openssl rand -hex 32` can generate a JWT secret; do not paste its output into reports.
3. To run without AI, leave `DEEPSEEK_API_KEY=` or set `DEEPSEEK_ENABLED=false`. Normal platform features remain available. Generation returns a clear unconfigured/disabled response without fabricated answers or successful history.
4. To enable real calls, the deployer supplies credentials from their own DeepSeek account in the local backend `.env` and sets `DEEPSEEK_ENABLED=true`. Keep keys out of frontend bundles, command arguments, screenshots and commits. Use `chmod 600 .env`; Git ignores the file.
5. `bash scripts/start-demo.sh` loads `.env`, builds and starts the backend using its macOS Temurin 17 setup. A manual `java -jar` does not load `.env` automatically: inject variables through deployment settings, or source a trusted local file using `set -a; source .env; set +a`. Restart the backend after configuration changes. Never initialize demo SQL in a shared business database.

The isolated 14001/14000 previews use backend 18080. Their launcher forcibly clears the provider key; editing the root `.env` does not enable paid calls in that environment.

## Supported environment variables

| Variable | Default | Meaning / accepted range |
| --- | --- | --- |
| `DEEPSEEK_ENABLED` | `true` | `false` disables generation even if a key is present |
| `DEEPSEEK_API_KEY` | empty | Backend only; no request when empty |
| `DEEPSEEK_BASE_URL` | `https://api.deepseek.com` | Only this official HTTPS root in normal operation |
| `DEEPSEEK_MODEL` | `deepseek-v4-flash` | Only currently accepted model |
| `DEEPSEEK_REASONING_EFFORT` | `high` | Only currently accepted value |
| `DEEPSEEK_MAX_TOKENS` | `4096` | Integer 1–8192; larger values reserve more budget |
| `DEEPSEEK_CONNECT_TIMEOUT_MS` | `5000` | 1–30000 milliseconds |
| `DEEPSEEK_READ_TIMEOUT_MS` | `120000` | 1–180000 milliseconds; no automatic retries |

Invalid values are rejected during initialization, even when generation is disabled. Only the Spring `test` profile permits local HTTP mocks; never enable it in production.

## Features and reference material

| Feature | Authorized context | Expected output |
| --- | --- | --- |
| Learning questions | Authenticated roles; current question, without automatic conversation context | Explanation, example and self-check; clarify missing conditions |
| Study plan | Student's enrolled courses and own progress | 1–90 days, 1–60 hours/week; tasks, time, outputs and review |
| Mistake analysis | Student's own assignment/exam answers, up to 20 questions | Evidence-based error hypotheses, correction and self-check; identify missing question text |
| Course summary | Student: enrolled courses and own progress; teacher: owned courses; admin: authorized course access | Distinguish an outline from a content summary; do not invent video contents or mastery |
| Lesson preparation | Teacher's courses or administrator-authorized course | 10–240 minutes, objectives, activities and formative assessment within the time budget |
| Teaching-data analysis | Teacher's own course/exam data or administrator scope | State sample/scope; distinguish facts, hypotheses and recommendations; no unsupported trends |

The selected UI language is sent in `Accept-Language` and added as an output-language instruction for new AI responses. Original reference material and saved responses are preserved. This is a prompt instruction, not a guarantee that every provider response follows the requested language.

Reference text cannot grant roles or access. Server-side authorization decides identity and ownership. The AI has no database-write, browser or homework-submission tools. Keyword checks catch only some obvious misuse; prompts cannot guarantee all model behavior.

## Quotas and logs

| Role | Daily requests | Daily token budget | Cooldown | Task-input character limit |
| --- | --- | --- | --- | --- |
| Student | 30 | 20,000 | 15 seconds | 1,000 |
| Teacher | 60 | 60,000 | 8 seconds | 2,500 |
| Administrator | 100 | 120,000 | 5 seconds | 4,000 |

Features share the account budget. Input limits include server-composed task text; task plus references are capped at 16,000 characters. The guard reserves an input estimate, configured output allowance and system-prompt margin before the call, then settles successful calls using provider usage. Estimates are neither an exact tokenizer nor a monetary cap; also configure vendor-side budgets.

Redis Lua atomically checks daily reservations, cooldowns and one active request per account. Multiple backend instances must share Redis and timezone. Deleting history does not refund quota. Existing successful daily usage provides a migration floor. Failed calls conservatively retain their request count and reservation to limit repeated failures. Disabled/unconfigured calls consume no request/token quota but retain a brief cooldown. Redis failure blocks provider calls. Expiring leases prevent abandoned requests from holding locks forever; an old request cannot release a new request's lock.

Production Redis needs persistence, access controls and capacity planning. Losing Redis loses protection counters not represented in successful history. The isolated demo Redis is not persistent. Daily boundaries follow the backend timezone.

Management usage totals describe successful history, not the conservative abuse-protection ledger. Successful answers and reference material are saved to the requesting user's AI history; only that user may inspect/delete them. Avoid secrets and irrelevant personal information in references sent to the provider.

MyBatis uses `NoLoggingImpl`. AI logs omit prompts, answers, references, keys, Authorization headers and hidden reasoning; exception logs record types only. Provider logs contain status, timing and usage metadata. Do not enable HTTP wire/header/body or SQL parameter logs to troubleshoot sensitive requests.

## Verification

Deterministic tests using local HTTP mocks, without paid calls:

```sh
mvn -f learn_server-master/pom.xml -pl ape-admin -am test
```

They cover endpoints, authorization, parameters, quota rejection, provider errors/timeouts, disabled/unconfigured zero-call behavior, usage and final-content validation. `scripts/verify-ai-quota.py` checks Lua concurrency and reservation boundaries only against the existing isolated Redis on 16379 and cleans up its test keys.

Real verification requires the deployer to explicitly enable calls and accept vendor costs. Sign in as the intended role, select an authorized course, submit one small task and inspect its answer, history and usage. Disable generation and restart afterward if appropriate. Credential errors, insufficient balance, rate limits, timeouts, missing usage and incomplete answers fail without fabricated success or automatic retry. Mock success does not replace real-provider acceptance.
