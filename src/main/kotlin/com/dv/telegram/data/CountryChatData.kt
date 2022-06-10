package com.dv.telegram.data

class CountryChatData(
    val countryName: String,
    val wordsString: String,
    val words: List<String>, // multiple chats possible for the same country are possible
    val chats: List<String>
) : BotAnswerData {
    val chatsAnswer: String = fillChatsAnswer() // pre-fill the bot answer to join the strings only once

    override fun isPresentIn(text: String): Boolean {
        if (text.isBlank()) {
            return false
        }

        for (word in words) {
            if (text.contains(word)) {
                return true
            }
        }

        return false
    }

    private fun fillChatsAnswer(): String {
        val chatsList = getChatLines()

        return "$countryName чаты:\n$chatsList" // %n will format as system-specific separator, see https://stackoverflow.com/questions/1883345/whats-up-with-javas-n-in-printf
    }

    private fun getChatLines(): String {
        val chatLines = chats.map { "— $it" }

        return chatLines.joinToString("\n")
    }
}
