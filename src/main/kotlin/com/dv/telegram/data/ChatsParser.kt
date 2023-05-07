package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.tabs.TabFormat
import org.apache.logging.log4j.kotlin.Logging

class ChatsParser : SheetDataParser<ChatData>, Logging {

    override fun getTabFormat(): TabFormat {
        return TabFormat.CHATS
    }

    override fun parse(rows: List<RowData>): List<ChatData> {
        val chatsData = mutableListOf<ChatData>()

        var rowNum = 1

        for (row in rows) {
            val chatLabel = row.getCellOrBlank(0)

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

            val chatData = ChatData(chatLabel, wordsString, words, chats)
            chatsData.add(chatData)

            logger.info("Row $rowNum: / $chatLabel / $words / $chats")
            logger.info(chatData.chatsAnswer)
            rowNum++
        }

        logger.info("Total ${chatsData.size} chats parsed.")

        return chatsData
    }
}
