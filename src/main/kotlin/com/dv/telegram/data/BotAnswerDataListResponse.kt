package com.dv.telegram.data

data class BotAnswerDataListResponse(
    val matchFound: Boolean,
    val matchedKeywords: List<String>,
    val responseText: String?
) {
    companion object { // factory methods
        fun noMatchFound() = BotAnswerDataListResponse(false, listOf(), null)

        fun matchFound(responseText: String, matchedKeywords: List<String>) =
            BotAnswerDataListResponse(true, matchedKeywords, responseText)
    }
}
