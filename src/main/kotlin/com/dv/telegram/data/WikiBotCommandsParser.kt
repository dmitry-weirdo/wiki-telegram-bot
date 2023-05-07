package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.google.WikiBotGoogleSheet
import com.dv.telegram.tabs.TabFormat
import org.apache.logging.log4j.kotlin.Logging

class WikiBotCommandsParser : SheetDataParser<WikiBotCommandData>, Logging {

    override fun getSheetData(sheet: WikiBotGoogleSheet) = sheet.commandsSheet

    override fun getTabFormat(): TabFormat {
        return TabFormat.COMMANDS
    }

    override fun parse(rows: List<RowData>): List<WikiBotCommandData> {
        val commands = mutableListOf<WikiBotCommandData>()

        var rowNum = 1

        for (row in rows) {
            val answer = row.getCellOrBlank(0)
            val wordsString = row.getCellOrBlank(1)
            val words = DataUtils.parseWords(wordsString)

            val command = WikiBotCommandData(answer, wordsString, words)
            commands.add(command)

            logger.info("Row $rowNum: / $answer / $words")
            rowNum++
        }

        logger.info("Total ${commands.size} commands parsed.")

        return commands
    }
}
