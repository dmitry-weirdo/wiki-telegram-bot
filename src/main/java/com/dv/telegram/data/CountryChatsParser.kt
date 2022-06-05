package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.google.WikiBotGoogleSheet
import org.apache.logging.log4j.kotlin.Logging

class CountryChatsParser : SheetDataParser<CountryChatData>, Logging { // mostly the same as CityChatsParser

    override fun getSheetData(sheet: WikiBotGoogleSheet) = sheet.countryChatsSheet

    override fun parse(rows: List<RowData>): List<CountryChatData> {
        val chatsData = mutableListOf<CountryChatData>()

        var rowNum = 1

        for (row in rows) {
            val countryName = row.getCellOrBlank(0)

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

            val countryChat = CountryChatData(countryName, wordsString, words, chats)
            chatsData.add(countryChat)

            logger.info("Row $rowNum: / $countryName / $words / $chats")
            logger.info(countryChat.chatsAnswer)
            rowNum++
        }

        logger.info("Total ${chatsData.size} country chats parsed.")

        return chatsData
    }
}
