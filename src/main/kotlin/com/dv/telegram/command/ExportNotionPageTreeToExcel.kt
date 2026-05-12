package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.excel.PageTreeXlsxWriter
import com.dv.telegram.exception.CommandException
import com.dv.telegram.notion.NotionPageNode
import com.dv.telegram.notion.NotionPageTree.fillRowsForExcelFile
import com.dv.telegram.notion.NotionWrapper
import com.dv.telegram.util.DateUtils
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.InputStream

class ExportNotionPageTreeToExcel : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText <notionPageId>` — экспорт дерева страниц Notion в Excel. В качестве начальной корневой страницы используется указанная страница."

    override val defaultCommandName = "/exportNotionPageTreeToExcel"

    override fun returnFileInResponse() = true

    override fun getResponseFileName(): String {
        val currentTimeFormatted = DateUtils.getCurrentDateTimeInFileNameFormat()
        return "notionPageTreeToExcel_$currentTimeFormatted.xlsx"
    }

    override fun getResponseFileCaption(): String {
        // todo: include page id and page name and start - stop times into response
        return "Экспорт дерева страниц Notion в Excel."
    }

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        return "fakeString"
    }

    override fun getFileContent(text: String, bot: WikiBot, update: Update): InputStream {
        val notionPageId = parseNotionPageId(text)
        if (notionPageId.isBlank()) {
            throw CommandException("Не указан `notionPageId`.\nФормат: `${bot.botName} $commandText <notionPageId>`.", true)
        }

        val result = NotionWrapper.collectPageTree(
            bot.notionToken,
            notionPageId,
            bot.notionImportTimeoutMinutes, // todo: we can use separate timeout for this operation
        )

        // todo: set the response data (start date, end date, diff) for getResponseFileCaption

//        val fakeTree = NotionPageTree.createFakePageTreeForTests()
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
