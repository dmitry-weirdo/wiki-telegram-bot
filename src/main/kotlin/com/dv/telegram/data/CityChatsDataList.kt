package com.dv.telegram.data

import com.dv.telegram.ResponseType

class CityChatsDataList(answers: List<CityChatData>, responseType: ResponseType) : BotAnswerDataList<CityChatData>(answers, responseType) {

    override fun getResponseText(matches: List<CityChatData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        return matches
            .joinToString("\n\n") { it.chatsAnswer } // passing transform parameter to joinToString
    }
}
