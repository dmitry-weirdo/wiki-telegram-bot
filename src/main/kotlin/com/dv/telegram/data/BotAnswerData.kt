package com.dv.telegram.data

/**
 * A data parsed from a single row in a Google Sheet bot config.
 * Multiple rows of a single tab are grouped into [BotAnswerDataList].
 */
interface BotAnswerData {
    fun isPresentIn(text: String): Boolean

    fun getMatch(text: String): BotAnswerMatch

    fun isPresentIn(text: String, keywords: List<String>): Boolean {
        if (text.isBlank()) {
            return false
        }

        for (keyword in keywords) {
            if (text.contains(keyword)) {
                return true
            }
        }

        return false
    }

    fun getMatch(text: String, keywords: List<String>): BotAnswerMatch {
        if (text.isBlank()) {
            return BotAnswerMatch.matchNotFound()
        }

        for (keyword in keywords) {
            if (text.contains(keyword)) {
                return BotAnswerMatch.matchFound(this, keyword)
            }
        }

        return BotAnswerMatch.matchNotFound()
    }
}
