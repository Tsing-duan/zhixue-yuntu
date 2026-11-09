# Chinese/English interfaces and demo content

[简体中文](internationalization.md) · **English** · [Project overview](../README.en.md)

Students, teachers and administrators can switch languages on sign-in pages and navigation. First use defaults to Simplified Chinese. `zhixue.locale` stores the local preference; the `zhixue_locale` cookie synchronizes frontends on the same host when focused. Different domains keep separate preferences. Element Plus uses the corresponding locale for dates, pagination and empty states. Switching does not remount pages or discard unsaved input.

## Maintenance

Each frontend's `src/i18n/index.ts` provides reactive `t()`, `$t` and `$locale`. Exact Chinese source strings are keys in the shared `shared/i18n/en.json` catalog. Translate UI labels, fixed-enum display labels and known system feedback only. Submitted enum values, routes and authorization identifiers remain unchanged. Do not run arbitrary user content through `$t`.

Add reviewed `中文|English` entries to `shared/i18n/translations.txt`, then run `python3 scripts/build-i18n-catalog.py`. It also checks the saved inventory; review generated JSON. Mark UI strings explicitly on new pages; do not bulk-replace business data or authorization constants. `localizeExamples` changes built-in examples until the first user edit, after which the input is preserved. Saved AI history remains unchanged; new response language is requested through the backend prompt.

## Demo boundaries

`VITE_DEMO_CONTENT` defaults to false. Set `VITE_DEMO_CONTENT=true` only in each frontend's ignored `.env.development.local` to enable the current synthetic sample mappings. Vite reloads configuration changes. This is a public display flag, never a place for credentials. Production builds do not read development files. Do not enable demo mode for real business environments.

`shared/i18n/demo-content.json` contains reviewed synthetic IDs, original field values and bilingual examples. A field changes display only when **both its ID and original value match**. Unknown IDs, new data and edited fields remain unchanged. No translation model is called and no database record is updated. A visible banner identifies the demo environment. Serialization and the request layer preserve unedited source values, including legacy forms that copy records. Explicit edits are preserved.

`scripts/build-demo-translations.py` reads only the isolated database recorded in `.run/modernization/state.json`, strictly checking its name and ports 13306/16379/18080. It cannot target arbitrary business databases. Regenerate and review mappings when synthetic fixtures change. This is a fixture snapshot, not a general database translator; never add real records to it.

## Validation and scope

Run `npm test --prefix learn_front-master` and `npm test --prefix learn_front_mange-master` to check preferences, editable examples, unchanged real records and safe serialization/submission values. `npm run build --prefix <frontend-directory>` checks types and builds each application. See [current verification evidence](verification-notes.en.md).

Original user posts, course material, uploads, external content and saved AI answers are not silently translated. Unknown backend business errors and user-defined menu names retain their source language; known system messages have English mappings. Historical architecture/verification documents remain in Chinese. Project overview, model setup and this guide provide linked Chinese/English editions.
