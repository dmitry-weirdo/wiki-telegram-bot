package com.dv.telegram.data

class CityChatsDataList(answers: List<CityChatData>) : BotAnswerDataList<CityChatData>(answers) {

    override fun getResponseText(matches: List<CityChatData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        return matches
            .joinToString("\n\n") { it.chatsAnswer } // passing transform parameter to joinToString
    }
}
