package com.dv.telegram

data class MessageProcessingResult(
    val messageIsForTheBot: Boolean,
    val isSpecialCommand: Boolean,
    val useMarkdown: Boolean,
    val answerIsFound: Boolean,
    val response: String?,
    var responseTypes: List<ResponseType>,
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
                responseTypes = listOf(ResponseType.SPECIAL_COMMAND)
            )
        }

        @JvmStatic
        fun answerFoundAsCommand(response: String): MessageProcessingResult {
            return answerFound(response, listOf(ResponseType.COMMAND))
        }

        @JvmStatic
        fun answerFound(response: String, responseTypes: List<ResponseType>): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = true,
                isSpecialCommand = false,
                useMarkdown = false,
                answerIsFound = true,
                response = response,
                responseTypes = responseTypes
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
                responseTypes = listOf(ResponseType.ANSWER_NOT_FOUND)
            )
        }
    }
}
