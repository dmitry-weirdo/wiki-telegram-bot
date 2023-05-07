package com.dv.telegram.data

import com.dv.telegram.ResponseType

/**
 * Finds the matches within the multiple rows from the same config tab
 * and groups them into one response string.
 */
abstract class BotAnswerDataList<T : BotAnswerData>(
    val answers: List<T>,
    val responseType: ResponseType
) {
    private fun findMatchedAnswers(text: String): List<T> { // returns list of answers
        return answers
            .filter { it.isPresentIn(text) }
    }

    private fun findMatches(text: String): List<BotAnswerMatch> { // returns list of matches including answers
        return answers
            .map { it.getMatch(text) }
            .filter { it.isPresent }
    }

    abstract fun getResponseText(matches: List<T>): String?

    fun getResponseText(text: String): String? {
        val matches = findMatchedAnswers(text)

        return getResponseText(matches)
    }

    @Suppress("UNCHECKED_CAST")
    fun getResponse(text: String): BotAnswerDataListResponse {
        val matches = findMatches(text)

        if (matches.isEmpty()) {
            return BotAnswerDataListResponse.noMatchFound(responseType)
        }

        // todo: maybe a nicer code (type it.answer to <T> somehow)
        val matchedAnswers = matches.map { it.answer!! as T }

        val responseText = getResponseText(matchedAnswers)
        if (responseText == null) { // super-safe - handle "there are matches, but no answers for these matches"
            return BotAnswerDataListResponse.noMatchFound(responseType)
        }

        val matchedKeywords = matches.map { it.matchedKeyword!! }

        return BotAnswerDataListResponse.matchFound(responseType, responseText, matchedKeywords)
    }
}
