package com.dv.telegram.data

class CountryChatsDataList(answers: List<CountryChatData>) : BotAnswerDataList<CountryChatData>(answers) {

    override fun getResponseText(matches: List<CountryChatData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        return matches
            .joinToString("\n\n") { it.chatsAnswer }
    }
}
