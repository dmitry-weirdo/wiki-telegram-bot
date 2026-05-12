---
name: Notion Tree Export Command
overview: Add a new admin Telegram special command that collects a Notion page tree from a provided source root page, appends it into a provided target block, exports the tree to Excel, and returns the generated `.xlsx` file with per-stage timing text in the response caption.
todos:
  - id: design-file-response-extension
    content: Design minimal response model extension to carry a file path for binary document replies while preserving existing text response behavior.
    status: pending
  - id: extract-npt-shared-service
    content: Extract/reuse Notion page-tree collect/append/export pipeline from NotionPageTree into a reusable service method returning timings and generated file metadata.
    status: pending
  - id: add-telegram-command
    content: Implement and register new special command `/notionPageTreeExport <sourceRootPageId> <targetBlockId>` with validation and user-facing response/caption text.
    status: pending
  - id: wire-telegram-document-send
    content: Update WikiBot send-document flow to stream binary file from path for `.xlsx` responses.
    status: pending
  - id: verify-and-regression-check
    content: Run targeted verification for command behavior, response timings, and no regressions in existing command processing.
    status: pending
isProject: false
---

# Implement Notion Tree Export Command

## Scope
Create a new special bot command with format:
- `<botName> /notionPageTreeExport <sourceRootPageId> <targetBlockId>`

Behavior:
1. Collect page tree from `sourceRootPageId`
2. Append tree entries into `targetBlockId` (block ID)
3. Export tree to Excel
4. Return the Excel file in Telegram response
5. Include execution times in response text for:
   - collect tree
   - append tree to Notion
   - export to Excel

## Reuse Existing Logic
Use existing tree logic from [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/notion/NotionPageTree.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/notion/NotionPageTree.kt) and collector in [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/notion/NotionPageTreeCollector.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/notion/NotionPageTreeCollector.kt):
- tree collection (`collectPagesTree`)
- append paragraph tree entries
- duration formatting
- Excel creation via [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/excel/PageTreeXlsxWriter.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/excel/PageTreeXlsxWriter.kt)

## Planned Changes
- Add new command class in `command` package (similar pattern to `CityChatsExportToNotion`) that:
  - parses `sourceRootPageId` and `targetBlockId`
  - calls a new Notion service wrapper method
  - returns summary text with timing values
  - marks `returnFileInResponse()` as `true`
  - provides file name and caption for Telegram document
- Register command in command list in [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/BotCommand.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/BotCommand.kt).

- Extract operational logic from `NotionPageTree` into reusable function(s) in `notion` package (new wrapper/object), e.g. `exportPageTreeToBlock(...)`, returning:
  - generated file path
  - generated file name
  - collect/append/export durations
  - total nodes/pages count
- Keep `NotionPageTree.main` runnable path intact while delegating to shared internal methods to avoid duplication.

- Extend file-response pipeline to support binary files from disk:
  - Current implementation sends `response` text bytes as document content in [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/WikiBot.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/WikiBot.kt), which is unsuitable for `.xlsx`.
  - Add a response path mechanism for special commands (e.g., response file path or bytes) in:
    - [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/BotCommand.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/BotCommand.kt)
    - [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/BasicBotCommand.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/BasicBotCommand.kt)
    - [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/SpecialCommandResponse.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/command/SpecialCommandResponse.kt)
    - [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/MessageProcessingResult.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/MessageProcessingResult.kt)
    - [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/WikiBotMessageProcessor.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/WikiBotMessageProcessor.kt)
    - [C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/WikiBot.kt](C:/java-vorwerk/java-u-x/wiki-telegram-bot/src/main/kotlin/com/dv/telegram/WikiBot.kt)
  - In `WikiBot.createSendDocument`, read from file path when provided and send actual binary stream with `.xlsx` filename.

## Response Format (planned)
Telegram caption/response text will include stage durations in a stable format, for example:
- `Collect tree: mm:ss`
- `Append to Notion: mm:ss`
- `Export to Excel: mm:ss`
- plus optional totals (root title, pages count).

## Validation Plan
- Unit-level command parsing checks for missing/invalid args.
- Manual test in private chat:
  - invoke with known source page + target block IDs
  - verify Notion block receives appended tree lines
  - verify bot returns downloadable `.xlsx`
  - verify file opens and row count matches collected nodes
  - verify caption includes all 3 timing segments.

- Regression sanity:
  - ensure existing file-returning commands (if any) still work
  - ensure regular text-only command responses are unchanged.