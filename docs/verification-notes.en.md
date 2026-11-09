# Verification and known limits

[简体中文](verification-notes.md) · **English**

Builds, unit tests, local browser behavior, real provider calls and production acceptance are distinct forms of evidence.

## 2026-11-09 update

- Both frontend type checks and Vite production builds passed. Large-bundle warnings remain; this is not a performance-optimization claim.
- Student: 14 unit tests; management: 20. Coverage includes sessions, editors, localized examples, user edits and canonical submitted values.
- Backend: 87 tests, including AI endpoints, authorization, parameter bounds, mock-provider behavior, disabled/unconfigured branches, log redaction and output-language instructions.
- Local isolated previews were checked across 51 route visits, 24 bilingual layouts, 3 preference-persistence groups and 16 real Chromium zoom cases from 100% to 200%. Additional checks cover empty/failure states, preserved form edits, major filters and narrow edit dialogs.
- The old student-facing 403 failures for teacher profiles and announcements are fixed through minimal-field read-only APIs. Live local role checks retained administrator writes, denied those writes to students/teachers, and excluded hidden teacher records and unpublished announcements.
- No real DeepSeek calls were made in this update. Vendor availability, balance, model availability, actual output language and answer quality were not revalidated.

Local screenshots, raw logs, databases and private material are excluded from publication. Browser evidence uses isolated synthetic fixtures, not every possible deployment. Scripts remain in `scripts/`; provide credentials locally. The About page also received Chinese/English checks on phone, tablet and desktop, preserving original logo proportions and adding no email address.

## Historical evidence

[AI/backend history](../PORTFOLIO_DEEPSEEK_V4_FLASH_E2E_REPORT.md) records an earlier 63-test run and two successful real-provider calls for student mistake analysis and teacher course health. [Dependency/build](../PORTFOLIO_DEPENDENCY_AUDIT_REPORT.md), [runtime baseline](../PORTFOLIO_RUNTIME_BASELINE_REPORT.md) and [older browser checks](../PORTFOLIO_BROWSER_E2E_REPORT.md) are historical snapshots, not claims that every check was rerun now.

Old Vue 2 EOL, Vue CLI and dynamic-route warnings describe the previous runtime. The current runtime uses Vue 3 / Vite / Router 4 with pinned Node/npm versions. Older audit results do not establish current dependency safety.

## Limits

- Detail, upload, exam-submission and CRUD-dialog paths were not exhaustively exercised. No load or production-deployment acceptance was performed.
- Some article/forum ownership paths and the full JWT/token attack surface still need review.
- Role/ownership 403 responses are intentional denials. Hiding a menu is not a security boundary.
- Original user content, editable menu/role/dictionary configuration and saved AI history remain unchanged. Some older backend mistake-group headings and chart labels without record IDs retain source text; full bilingual data coverage is not claimed.
- Users must assess AI output. Prompts/keyword checks cannot guarantee model behavior; token estimates are not monetary caps.
- The adapter currently supports a pinned official DeepSeek protocol. Mock success is not real-provider acceptance.
