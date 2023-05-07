package com.dv.telegram.data

import com.dv.telegram.ResponseType

data class BotAnswerDataListResponse(
    val matchFound: Boolean,
    val matchedKeywords: List<String>,
    val responseType: ResponseType,
    val responseText: String?
) {
    companion object { // factory methods
        fun noMatchFound(responseType: ResponseType) = BotAnswerDataListResponse(
            false,
            listOf(),
            responseType,
            null
        )

        fun matchFound(responseType: ResponseType, responseText: String, matchedKeywords: List<String>) =
            BotAnswerDataListResponse(
                true,
                matchedKeywords,
                responseType,
                responseText
            )
    }
}
