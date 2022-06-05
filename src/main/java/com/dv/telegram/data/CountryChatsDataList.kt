package com.dv.telegram.data

import com.dv.telegram.GoogleSheetBotData

class CountryChatsDataList(answers: List<CountryChatData>) : BotAnswerDataList<CountryChatData>(answers) {

    constructor(botData: GoogleSheetBotData) : this(botData.countryChats)

    override fun getResponseText(matches: List<CountryChatData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        return matches
            .joinToString("\n\n") { it.chatsAnswer }
    }
}
