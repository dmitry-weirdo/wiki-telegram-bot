package com.dv.telegram.data

data class BotAnswerDataListResponse(
    val matchFound: Boolean,
    val responseText: String?,
    val matchedKeywords: List<String>
) {
    companion object { // factory methods
        fun noMatchFound() = BotAnswerDataListResponse(false, null, listOf())

        fun matchFound(responseText: String, matchedKeywords: List<String>) =
            BotAnswerDataListResponse(true, responseText, matchedKeywords)
    }
}
