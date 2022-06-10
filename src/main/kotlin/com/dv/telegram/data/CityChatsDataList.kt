package com.dv.telegram.data

import com.dv.telegram.GoogleSheetBotData

class CityChatsDataList(answers: List<CityChatData>) : BotAnswerDataList<CityChatData>(answers) {

    constructor(botData: GoogleSheetBotData) : this(botData.cityChats)

    override fun getResponseText(matches: List<CityChatData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        return matches
            .joinToString("\n\n") { it.chatsAnswer } // passing transform parameter to joinToString
    }
}
