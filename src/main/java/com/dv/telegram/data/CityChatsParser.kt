package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.google.WikiBotGoogleSheet
import org.apache.logging.log4j.kotlin.Logging

class CityChatsParser : SheetDataParser<CityChatData>, Logging {

    override fun getSheetData(sheet: WikiBotGoogleSheet) = sheet.cityChatsSheet

    override fun parse(rows: List<RowData>): List<CityChatData> {
        val chatsData = mutableListOf<CityChatData>()

        var rowNum = 1

        for (row in rows) {
            val cityName = row.getCellOrBlank(0)

            // words
            val wordsString = row.getCellOrBlank(1)
            val words = DataUtils.parseWords(wordsString)

            // chats
            val chats = mutableListOf<String>()
            for (cellNum in 2 until row.cells.size) {
                val chat = row.getCellOrBlank(cellNum)
                if (chat.isNotBlank()) {
                    chats.add(chat)
                }
            }

            val cityChat = CityChatData(cityName, wordsString, words, chats)
            chatsData.add(cityChat)

            logger.info("Row $rowNum: / $cityName / $words / $chats")
            logger.info(cityChat.chatsAnswer)
            rowNum++
        }

        logger.info("Total ${chatsData.size} city chats parsed.")

        return chatsData
    }
}
