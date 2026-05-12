---
name: BotCommand getFileContent
overview: Add `BotCommand.getFileContent` (UTF-8 `ByteArrayInputStream` when `returnFileInResponse()`, else null), thread the resulting `InputStream?` through `SpecialCommandResponse` and `MessageProcessingResult`, and have `WikiBot.createSendDocument` prefer that stream. Use an optional precomputed response string parameter so `BotSpecialCommands` does not invoke `getResponse` twice for file commands (binary overrides can ignore the cached string).
todos:
  - id: bot-command-method
    content: Add getFileContent to BotCommand (imports; default body; optional precomputed response body for single-pass plumbing)
    status: pending
  - id: special-command-response
    content: Extend SpecialCommandResponse with responseFileContent InputStream?, factories and hasResponse semantics
    status: pending
  - id: bot-special-commands
    content: Wire BotSpecialCommands.getResponse to populate responseFileContent for file-returning commands
    status: pending
  - id: message-processing-result
    content: Add responseFileContent to MessageProcessingResult and specialCommand factory overload used by WikiBotMessageProcessor
    status: pending
  - id: wiki-bot-send-document
    content: Prefer processingResult.responseFileContent in createSendDocument; fallback encode string only if stream null
    status: pending
isProject: false
---

# `getFileContent` + `InputStream` on `SpecialCommandResponse`

## Context

- Today the document body is built only in [`WikiBot.createSendDocument`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\WikiBot.kt) from `processingResult.getResponseOrFail()` as UTF-8 bytes.
- [`BotSpecialCommands.getResponse`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\BotSpecialCommands.kt) returns [`SpecialCommandResponse`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\SpecialCommandResponse.kt) with text + file flags + name + caption; no stream yet.
- File-returning commands today: [`AllBotsGetSuccessfulRequests`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\AllBotsGetSuccessfulRequests.kt), [`AllBotsGetFailedRequests`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\AllBotsGetFailedRequests.kt).

## 1. `BotCommand.getFileContent`

In [`BotCommand.kt`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\BotCommand.kt), add (imports: `ByteArrayInputStream`, `InputStream`, `StandardCharsets`):

- **`fun getFileContent(text: String, bot: WikiBot, update: Update, precomputedResponseBody: String? = null): InputStream?`**
  - If `!returnFileInResponse()` → `null`.
  - Else `val body = precomputedResponseBody ?: getResponse(text, bot, update)` then `ByteArrayInputStream(body.toByteArray(StandardCharsets.UTF_8))` (matches current `createSendDocument` encoding).
  - **Why the extra argument:** [`BotSpecialCommands`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\BotSpecialCommands.kt) already computes `response` once; passing it avoids calling `getResponse` twice when using the default UTF-8 file body. Commands that attach **binary** content later override `getFileContent` and can **ignore** `precomputedResponseBody`.

Place the method beside the other file-related declarations.

## 2. `SpecialCommandResponse`

In [`SpecialCommandResponse.kt`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\SpecialCommandResponse.kt):

- Add **`val responseFileContent: InputStream?`** (typically `null` when `returnFileInResponse == false`).
- **`noResponse()`**: `responseFileContent = null`.
- **`withResponse(...)` (two-arg text path):** keep `responseFileContent = null`.
- **`withResponse(..., returnFileInResponse, fileName, caption)`:** add parameter **`responseFileContent: InputStream?`** — when `returnFileInResponse`, pass the stream from command (see §3); when false, pass `null`.
- **`hasResponse()`**: unchanged in spirit (still keyed off non-blank `response`); file commands remain text-backed for captions/logging.

## 3. `BotSpecialCommands`

In the branch where `command.returnFileInResponse()` ([`BotSpecialCommands.kt`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\BotSpecialCommands.kt) ~58–65):

1. Keep **`val response = command.getResponse(...)`** once.
2. **`val stream = command.getFileContent(text, bot, update, precomputedResponseBody = response)`** (non-null stream for current file commands; command contract may tighten later so file responses require non-null stream when `returnFileInResponse`).
3. Extend **`SpecialCommandResponse.withResponse(...)`** call with **`responseFileContent = stream`**.

## 4. `MessageProcessingResult`

In [`MessageProcessingResult.kt`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\MessageProcessingResult.kt):

- Add **`val responseFileContent: InputStream?`** (default `null` everywhere except the special-command-with-file factory).
- Extend **`specialCommand(..., returnFileInResponse, responseFileName, responseFileCaption)`** to accept **`responseFileContent: InputStream?`** and copy it from [`WikiBotMessageProcessor`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\WikiBotMessageProcessor.kt) when building the result (pass `specialCommandResponse.responseFileContent`).

## 5. `WikiBot.createSendDocument`

In [`WikiBot.kt`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\WikiBot.kt):

- Prefer **`processingResult.responseFileContent`** when non-null for `InputFile(..., fileName)`.
- If null (defensive / legacy path), keep current behavior: **`ByteArrayInputStream(processingResult.getResponseOrFail().toByteArray(StandardCharsets.UTF_8))`**.

## Tests

- [`BotGetResponseTest.kt`](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\test\kotlin\com\dv\telegram\BotGetResponseTest.kt): adjust only if tests assert construction of `SpecialCommandResponse` / `MessageProcessingResult` field counts; otherwise no change.

## Architectural alternative (sealed split: text vs document)

Deferred: a `sealed` split of command types still makes sense if many **binary** attachments appear; the current plan keeps one `BotCommand` type and uses `getFileContent` overrides for non-UTF-8 bodies.

## Extension point

Future commands that return **binary** files override **`getFileContent(..., precomputedResponseBody)`** and build the stream from disk/workbook, ignoring the cached string when appropriate.
