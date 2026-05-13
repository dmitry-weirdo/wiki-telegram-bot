---
name: detailed-response-caption
overview: Add command-level operation context for Notion page tree export and use it to build a detailed file caption with page info, start/end timestamps, and human-readable duration.
todos:
  - id: add-export-context
    content: Add operation context storage in ExportNotionPageTreeToExcel and populate it in getFileContent
    status: pending
  - id: caption-from-context
    content: Construct getResponseFileCaption from saved context with requested multiline Russian format
    status: pending
  - id: date-duration-formatting
    content: Use/extend DateUtils to format start/end timestamps and duration as X мин Y сек
    status: pending
  - id: verify-build
    content: Run build/tests and validate caption output format
    status: pending
isProject: false
---

# Plan: Detailed Export Caption for Notion Tree

## What will change
- Extend `ExportNotionPageTreeToExcel` to persist operation context produced in `getFileContent` and consume it in `getResponseFileCaption`.
- Build a multiline caption in the requested format:
  - `Экспорт дерева страницы <pageId> ("<pageTitle>") в Excel.`
  - `Начало операции: <formatted startTime>`
  - `Конец  операции: <formatted endTime>`
  - `Длительность: X мин Y сек`

## Target files
- [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/ExportNotionPageTreeToExcel.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/ExportNotionPageTreeToExcel.kt)
- [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/util/DateUtils.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/util/DateUtils.kt)

## Implementation steps
- In `ExportNotionPageTreeToExcel`, add a small private context object (e.g., data class + nullable field) to store:
  - `rootPageId`, `rootPageTitle`, `startTime`, `endTime` (from `NotionPageTreeCollectResult`).
- In `getFileContent`, after `NotionWrapper.collectPageTree(...)`, save this context field before generating XLSX.
- In `getResponseFileCaption`, build caption from saved context:
  - convert `startTime`/`endTime` millis into date-time values;
  - format start/end via existing Notion-style formatter in `DateUtils`;
  - render duration in Russian `X мин Y сек`.
- Add/adjust a date utility in `DateUtils` if needed to avoid duplicating millis-to-formatted-text logic.
- Keep a safe fallback caption when context is unavailable (defensive behavior).

## Validation
- Run project tests/build for the changed module.
- Manually verify that command response file caption now includes page id/title, start/end time, and duration in the exact requested shape.