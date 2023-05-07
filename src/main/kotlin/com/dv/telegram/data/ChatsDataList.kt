package com.dv.telegram.data

import com.dv.telegram.ResponseType

class ChatsDataList(answers: List<ChatData>, responseType: ResponseType) : BotAnswerDataList<ChatData>(answers, responseType) {

    override fun getResponseText(matches: List<ChatData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        return matches
            .joinToString("\n\n") { it.chatsAnswer } // passing transform parameter to joinToString
    }
}
