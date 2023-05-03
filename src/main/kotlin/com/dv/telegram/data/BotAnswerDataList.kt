package com.dv.telegram.data

/**
 * Finds the matches within the multiple rows from the same config tab
 * and groups them into one response string.
 */
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
