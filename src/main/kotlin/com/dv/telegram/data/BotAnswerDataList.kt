package com.dv.telegram.data

import com.dv.telegram.ResponseType
import com.dv.telegram.tabs.TabData
import org.apache.logging.log4j.kotlin.Logging

/**
 * Finds the matches within the multiple rows from the same config tab
 * and groups them into one response string.
 */
abstract class BotAnswerDataList<T : BotAnswerData>(
    val answers: List<T>,
    private val responseType: ResponseType,
    private val showHeader: Boolean,
    private val header: String?
) : Logging {
    constructor(
        answers: List<T>,
        tabData: TabData
    ) : this(
        answers,
        tabData.responseType,
        tabData.tabConfig.showHeader,
        tabData.tabConfig.header
    )

    @Suppress("UNCHECKED_CAST")
    fun getResponse(text: String): BotAnswerDataListResponse {
        val matches = findMatches(text)

        if (matches.isEmpty()) {
            return BotAnswerDataListResponse.noMatchFound(responseType)
        }

        // todo: maybe a nicer code (typed-cast it.answer to <T> somehow)
        val matchedAnswers = matches.map { it.answer!! as T }

        val responseText = getResponseText(matchedAnswers)
        if (responseText.isNullOrBlank()) { // super-safe - handle "there are matches, but no answers for these matches"
            return BotAnswerDataListResponse.noMatchFound(responseType)
        }

        val responseTextWithHeader = addHeader(responseText)

        val matchedKeywords = matches.map { it.matchedKeyword!! }

        return BotAnswerDataListResponse.matchFound(responseType, responseTextWithHeader, matchedKeywords)
    }

    private fun findMatches(text: String): List<BotAnswerMatch> { // returns list of matches including answers
        return answers
            .map { it.getMatch(text) }
            .filter { it.isPresent }
    }

    abstract fun getResponseText(matches: List<T>): String?

    private fun addHeader(responseText: String): String { // expects not-null and non-blank responseText
        if (!showHeader) {
            return responseText
        }

        if (header.isNullOrBlank()) { // pre-cautious for incorrect configuration
            logger.warn("showHeader is set to ${true} but header is null or blank. Do not add the header.")
            return responseText
        }

        return "==== $header ====\n$responseText"
    }
}
