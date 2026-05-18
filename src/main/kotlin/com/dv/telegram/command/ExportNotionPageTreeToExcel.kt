package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.excel.PageTreeXlsxWriter
import com.dv.telegram.exception.CommandException
import com.dv.telegram.notion.NotionPageNode
import com.dv.telegram.notion.NotionPageTree.fillRowsForExcelFile
import com.dv.telegram.notion.NotionPageTreeCollectResult
import com.dv.telegram.notion.NotionWrapper
import com.dv.telegram.util.DateUtils
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.InputStream

class ExportNotionPageTreeToExcel : BasicBotCommand() {
    companion object {
        const val CONTEXT_KEY = "exportNotionPageTreeResult"
    }

    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText <notionPageId>` — экспорт дерева страниц Notion в Excel. В качестве начальной корневой страницы используется указанная страница."

    override val defaultCommandName = "/exportNotionPageTreeToExcel"

    override fun returnFileInResponse() = true

    override fun getResponseFileName(context: BotContext): String {
        val result = context.getOrFail<NotionPageTreeCollectResult>(CONTEXT_KEY)

        val currentTimeFormatted = DateUtils.getCurrentDateTimeInFileNameFormat()

        return "notionPageTree_${result.rootPageId}_$currentTimeFormatted.xlsx"
    }

    override fun getResponseFileCaption(context: BotContext): String {
        val result = context.getOrFail<NotionPageTreeCollectResult>(CONTEXT_KEY)
        val startFormatted = DateUtils.getDateTimeInNotionOperationFormat(result.startTime)
        val endFormatted = DateUtils.getDateTimeInNotionOperationFormat(result.endTime)
        val duration = DateUtils.formatDurationForBotMessage(result.startTime, result.endTime)
        
        return """Экспорт дерева страницы ${result.rootPageId} ("${result.rootPageTitle}") в Excel.

Начало операции: $startFormatted
Конец операции: $endFormatted
Длительность: $duration"""
    }

    override fun getResponse(text: String, bot: WikiBot, update: Update, context: BotContext): String {
        return "fakeString"
    }

    override fun getFileContent(text: String, bot: WikiBot, update: Update, context: BotContext): InputStream {
        val notionPageId = parseNotionPageId(text)
        if (notionPageId.isBlank()) {
            throw CommandException("Не указан `notionPageId`.\nФормат: `${bot.botName} $commandText <notionPageId>`.", true)
        }

        val result = NotionWrapper.collectPageTree(
            bot.notionToken,
            notionPageId,
            bot.notionImportTimeoutMinutes, // todo: we can use separate timeout for this operation
        )

        context.put(CONTEXT_KEY, result)

        val nodes = mutableListOf<NotionPageNode>()
        fillRowsForExcelFile(result.root, nodes)

        val workbook = PageTreeXlsxWriter.createWorkbook(nodes)
        return PageTreeXlsxWriter.workbookToInputStream(workbook)
    }

    private fun parseNotionPageId(text: String): String {
        val commandStartIndex = text.indexOf(commandText)
        if (commandStartIndex < 0) {
            return ""
        }

        val commandEndIndex = commandStartIndex + commandText.length
        if (commandEndIndex >= text.length) {
            return ""
        }

        return text.substring(commandEndIndex).trim()
    }
}
