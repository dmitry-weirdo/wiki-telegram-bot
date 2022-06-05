package com.dv.telegram.data

data class WikiBotCommandData(
    val answer: String,
    val wordsString: String,
    val words: List<String>
) {
    fun isPresentIn(text: String): Boolean {
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

    fun getOneLineAnswer() = answer

    fun getMultiLineAnswer() = "— $answer"
}
