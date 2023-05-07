package com.dv.telegram.data

import com.dv.telegram.ResponseType

class CountryChatsDataList(answers: List<CountryChatData>, responseType: ResponseType) : BotAnswerDataList<CountryChatData>(answers, responseType) {

    override fun getResponseText(matches: List<CountryChatData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        return matches
            .joinToString("\n\n") { it.chatsAnswer }
    }
}
