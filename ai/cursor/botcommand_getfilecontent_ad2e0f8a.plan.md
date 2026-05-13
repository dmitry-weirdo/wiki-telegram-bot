---
name: BotCommand getFileContent
overview: Commands that return files own String→UTF-8 InputStream conversion in their implementations. BotCommand exposes getFileContent with default null. SpecialCommandResponse and MessageProcessingResult carry the InputStream. WikiBot.createSendDocument sends only that stream — no string re-encoding fallback and no framework “precomputed response” parameter.
todos:
  - id: bot-command-method
    content: BotCommand — getFileContent(text,bot,update) returns InputStream?, default null (no UTF-8 default on interface)
    status: pending
  - id: file-command-impls
    content: AllBotsGetSuccessfulRequests + AllBotsGetFailedRequests — override getFileContent with UTF-8 BAIS; internal helper so body matches getResponse without duplicating logic twice per request
    status: pending
  - id: special-command-response
    content: SpecialCommandResponse — responseFileContent InputStream?, update factories (text path null stream)
    status: pending
  - id: bot-special-commands
    content: BotSpecialCommands — populate responseFileContent via command.getFileContent (no extra parameters)
    status: pending
  - id: message-processing-result
    content: MessageProcessingResult — responseFileContent through specialCommand(...) and WikiBotMessageProcessor
    status: pending
  - id: wiki-bot-send-document
    content: WikiBot.createSendDocument — use only processingResult.responseFileContent; fail if missing when returning a file (no fallback from String)
    status: pending
isProject: false
---

# `getFileContent` owned by commands; stream only in `WikiBot`

## Principles (per product direction)

- **No** converting `String` → `InputStream` inside [**`WikiBot.createSendDocument`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\WikiBot.kt). The attachment bytes come only from **`MessageProcessingResult.responseFileContent`** (ultimately **`SpecialCommandResponse.responseFileContent`**).
- **No** “precomputed response body” (or similar) on **`getFileContent`** — that stays out of **`WikiBot`** and off the **`BotCommand`** API. Avoiding duplicated work stays **inside** each command implementation (shared private builder, etc.).
- **Concrete file commands** are responsible for how the document body is built (today: UTF‑8 bytes of their text payload; tomorrow: arbitrary binary overrides).

## 1. `BotCommand.getFileContent`

In [**`BotCommand.kt`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\BotCommand.kt):

- Add **`fun getFileContent(text: String, bot: WikiBot, update: Update): InputStream?`**
- **Default:** `null` (Kotlin interface default implementation).

Only commands with **`returnFileInResponse() == true`** override to return a non-null stream.**No** UTF‑8 **`ByteArrayInputStream`** helper on the interface itself.

Imports: **`java.io.InputStream`** only on the interface file (implementations pull in **`ByteArrayInputStream`** / **`StandardCharsets`** where needed).

## 2. File command classes (implementations)

[**`AllBotsGetSuccessfulRequests`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\AllBotsGetSuccessfulRequests.kt) and [**`AllBotsGetFailedRequests`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\AllBotsGetFailedRequests.kt):

- **`getResponse`** and **`getFileContent`** both use the **same** internal helper (e.g. `private fun buildFileBody(...) : String`) so [**`BotSpecialCommands`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\BotSpecialCommands.kt) can call **`getResponse`** then **`getFileContent`** without building two different payloads or invoking heavy logic twice.
- **`override fun getFileContent(...)`**: `ByteArrayInputStream(buildFileBody(...).toByteArray(StandardCharsets.UTF_8))` — same encoding that **`createSendDocument`** used historically.

Future document commands replace **`getFileContent`** with workbook/disk/other streams without changing **`WikiBot`**.

## 3. `SpecialCommandResponse`

In [**`SpecialCommandResponse.kt`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\command\SpecialCommandResponse.kt):

- **`val responseFileContent: InputStream?`**
- **`noResponse()`** / text-only **`withResponse`**: `null`
- File **`withResponse(...)`**: callers pass **`responseFileContent`** from **`command.getFileContent(...)`**. When **`returnFileInResponse`**, it should be **non-null** for current commands (enforced implicitly or with **`require`** in factory if you want a hard invariant).

## 4. `BotSpecialCommands`

In the file branch (~58–65):

1. **`val response = command.getResponse(...)`**
2. **`val stream = command.getFileContent(text, bot, update)`** — expect **`stream != null`** for current file-returning implementations.
3. Pass **`responseFileContent = stream`** into **`SpecialCommandResponse.withResponse`**.

## 5. `MessageProcessingResult`

In [**`MessageProcessingResult.kt`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\MessageProcessingResult.kt):

- **`val responseFileContent: InputStream?`**
- Thread through **`specialCommand(...)`** overload and [**`WikiBotMessageProcessor`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\main\kotlin\com\dv\telegram\WikiBotMessageProcessor.kt) from **`specialCommandResponse.responseFileContent`**

## 6. `WikiBot.createSendDocument`

- **`val inputStream = processingResult.responseFileContent ?: error("...")`** (or a small **`WikiBotException`** message) — **no** **`getResponseOrFail().toByteArray`** branch.
- Remove **`StandardCharsets`** import from **`WikiBot.kt`** only if nothing else uses it there after the change.

## Tests

[**`BotGetResponseTest.kt`**](c:\java-vorwerk\java-u-x\wiki-telegram-bot\src\test\kotlin\com\dv\telegram\BotGetResponseTest.kt): update if anything constructs **`SpecialCommandResponse`** / **`MessageProcessingResult`** with the new field.

## Architectural alternative (sealed split)

Unchanged: consider a sealed **text vs document** command model when many binary exports exist.
