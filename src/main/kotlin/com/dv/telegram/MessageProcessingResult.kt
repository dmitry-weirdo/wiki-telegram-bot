package com.dv.telegram

import com.dv.telegram.data.BotAnswerDataListResponse

data class MessageProcessingResult(
    val messageIsForTheBot: Boolean,
    val isSpecialCommand: Boolean,
    val useMarkdown: Boolean,
    val answerIsFound: Boolean,
    val response: String?,
    val responseTypes: List<ResponseType>,
    val matchedKeywords: List<String>
) {
    fun hasNoResponse(): Boolean {
        return response == null
    }

    fun getResponseOrFail(): String {
        return response ?: error("getResponse called on an empty response.")
    }

    companion object { // factory methods
        @JvmStatic
        fun notForTheBot(): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = false,
                isSpecialCommand = false,
                useMarkdown = false,
                answerIsFound = false,
                response = null,
                responseTypes = listOf(ResponseType.NOT_FOR_THE_BOT),
                matchedKeywords = listOf()
            )
        }

        @JvmStatic
        fun specialCommand(response: String?, useMarkdown: Boolean): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = true,
                isSpecialCommand = true,
                useMarkdown = useMarkdown,
                answerIsFound = true,
                response = response,
                responseTypes = listOf(ResponseType.SPECIAL_COMMAND),
                matchedKeywords = listOf() // todo: think about this, maybe set command name?
            )
        }

        @JvmStatic
        fun answerFoundAsCommand(response: BotAnswerDataListResponse): MessageProcessingResult {
            return answerFoundAsCommand(response.responseText!!, response.matchedKeywords)
        }

        @JvmStatic
        fun answerFoundAsCommand(response: String, matchedKeywords: List<String>): MessageProcessingResult {
            return answerFound(response, listOf(ResponseType.COMMAND), matchedKeywords)
        }

        @JvmStatic
        fun answerFound(response: String, responseTypes: List<ResponseType>, matchedKeywords: List<String>): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = true,
                isSpecialCommand = false,
                useMarkdown = false,
                answerIsFound = true,
                response = response,
                responseTypes = responseTypes,
                matchedKeywords = matchedKeywords
            )
        }

        @JvmStatic
        fun answerNotFound(response: String?): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = true,
                isSpecialCommand = false,
                useMarkdown = false,
                answerIsFound = false,
                response = response,
                responseTypes = listOf(ResponseType.ANSWER_NOT_FOUND),
                matchedKeywords = listOf()
            )
        }
    }
}
