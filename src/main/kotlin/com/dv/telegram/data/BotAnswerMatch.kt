package com.dv.telegram.data

data class BotAnswerMatch (
    val isPresent: Boolean,
    val matchedKeyword: String?,
    val answer: BotAnswerData?
) {
    companion object { // factory methods
        fun matchFound(answer: BotAnswerData, matchedKeyword: String) = BotAnswerMatch(true, matchedKeyword, answer)

        fun matchNotFound() = BotAnswerMatch(false, null, null)
    }
}
