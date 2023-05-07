package com.dv.telegram.data

data class BotAnswerMatch (
    val isPresent: Boolean,
    val matchedKeyword: String?
) {
    companion object { // factory methods
        fun matchFound(matchedKeyword: String) = BotAnswerMatch(true, matchedKeyword)

        fun matchNotFound() = BotAnswerMatch(false, null)
    }
}
