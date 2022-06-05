package com.dv.telegram.data

abstract class BotAnswerDataList<T : BotAnswerData>(
    val answers: List<T>
) {
    fun findMatches(text: String): List<T> {
        return answers
            .filter { it.isPresentIn(text) }
    }

    abstract fun getResponseText(matches: List<T>): String?

    fun getResponseText(text: String): String? {
        val matches = findMatches(text)

        return getResponseText(matches)
    }
}
