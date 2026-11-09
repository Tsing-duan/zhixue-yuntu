# Zhixue Cloud

[简体中文](README.md) · **English**

An online teaching project combining courses, assignments, exams, learner discussions and optional AI assistance for students, teachers and administrators.

## Features

- **Students:** discover courses and teachers, study chapters, complete assignments and exams, review mistakes, and share notes/discussions. Teacher profiles and announcements are read-only.
- **Teachers:** manage their own courses, chapters, assignments and exams; inspect teaching data and organize lesson materials with AI.
- **Administrators:** manage users, roles, menus and platform content; inspect successful AI requests and token usage.
- **AI assistance:** learning questions, study plans, mistake analysis, course summaries, lesson preparation and teaching-data analysis. The server checks authorization, ownership, input limits, quotas and cooldowns.
- **Chinese/English:** Simplified Chinese by default; switch on sign-in pages or navigation, with the choice preserved on refresh. Original user content and saved AI answers remain unchanged. Explicit demo mode provides labeled bilingual synthetic examples.

The home page surfaces actual courses, teachers and shared content. Navigation adapts to narrow windows and zoom; short pages keep the footer near the viewport bottom. The About page includes project/personal marks, a brief introduction and [GitHub feedback](https://github.com/Tsing-duan/zhixue-yuntu/issues).

## Origins and continued maintenance

This project builds on [WuJunJie521/RuoYi](https://github.com/WuJunJie521/RuoYi). The original teaching modules were created by a course team. Qingduan continues maintenance, fixes business/authorization issues, and uses AI assistance for teaching features and engineering improvements. It is not presented as code written entirely independently from scratch. Development has used Codex and CC-DS assistance.

This update migrates both frontends from Vue 2 / Vue CLI to Vue 3 / Vite, adds Pinia session management, Element Plus and bilingual UI, and improves student read-only APIs, AI usage protection and task-specific prompts. Original team contributions and upstream licensing are retained.

## Technology

| Layer | Stack |
| --- | --- |
| Frontend | Vue 3, Vite, TypeScript in the core layer, Pinia, Vue Router 4, Element Plus, Axios, ECharts, Quill 2 |
| Backend | Spring Boot, MyBatis-Plus, Shiro, JWT |
| Data | MySQL, Redis |
| AI | Official DeepSeek, pinned model and Chat Completions protocol, non-streaming calls |
| Verification | JUnit / MockMvc, Vitest, Playwright and isolated authorization checks |

Existing business pages largely retain Options API / JavaScript; this is not a full TypeScript rewrite.

## Run locally

Use Java 17, Maven, MySQL, Redis, Node 26.12.0 and npm 11.20.0. The launcher currently assumes macOS with Temurin 17. Follow the [local setup guide](docs/local-development.en.md).

**Demo SQL drops and recreates tables: import it only into a new, disposable database.** This public repository contains no ready-to-use login passwords, local databases, runtime uploads or `.env` files.

```sh
npm ci --prefix learn_front-master
npm ci --prefix learn_front_mange-master
# After creating the demo database and configuring the backend:
bash scripts/start-demo.sh
```

Default student sign-in: `http://localhost:3001/login`; teacher/admin sign-in: `http://localhost:3000/login`. Frontend builds include type checking. Deployment requires history-route fallback.

## Connect a model

Normal teaching features work without a model key. The current adapter supports official DeepSeek only: `https://api.deepseek.com`, `deepseek-v4-flash` and `reasoning_effort=high` are code constraints, not a generic adapter for arbitrary OpenAI-compatible services.

Set `DEEPSEEK_API_KEY` in the backend's ignored local `.env` or deployment environment. An empty key produces an unconfigured response; `DEEPSEEK_ENABLED=false` disables generation. Never put credentials in frontend settings or commits. See [AI setup](docs/ai-model-setup.en.md) for supported settings, context boundaries, Redis protection and verification. This update made no real provider calls; mock success is not proof of vendor availability.

## Verification and limits

Both frontend builds, 34 frontend tests and 87 backend tests passed. Local browser checks covered 51 route visits, 24 bilingual layouts, 3 preference-persistence groups and 16 real browser zoom cases. See [verification notes](docs/verification-notes.en.md) for scope and exclusions. These are local checks, not full production acceptance.

The old student-facing 403 failures for teacher profiles and announcements are addressed by minimal-field read-only endpoints. Students still cannot perform administrator writes. Other role/ownership 403 responses are intentional denials.

This remains a learning/demo project. Some article/forum ownership paths, the full token attack surface and all business dialogs have not been exhaustively reviewed. A few older backend grouping/chart labels retain their original language. AI output needs human judgment; do not deploy to production without further validation.

## Documentation and feedback

- [Local setup](docs/local-development.en.md) · [AI setup](docs/ai-model-setup.en.md)
- [Localization/data boundaries](docs/internationalization.en.md) · [Verification](docs/verification-notes.en.md)
- [Architecture (Chinese)](docs/architecture.md) · [AI request flow (Chinese)](docs/ai-vertical-slice.md)
- [GitHub Issues](https://github.com/Tsing-duan/zhixue-yuntu/issues): do not post credentials or real student data.

## License

[MIT License](LICENSE), retaining the upstream RuoYi copyright and permission notice.

Legacy menu icon fonts retain the [Element UI MIT notice](docs/modernization/element-icons-LICENSE).
