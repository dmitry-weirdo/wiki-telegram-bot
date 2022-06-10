package com.dv.telegram

data class MessageProcessingResult(
    val messageIsForTheBot: Boolean,
    val isSpecialCommand: Boolean,
    val useMarkdown: Boolean,
    val answerIsFound: Boolean,
    val response: String?
) {
    fun hasNoResponse(): Boolean {
        return response == null
    }

    fun getResponseOrFail(): String {
        return response ?: throw IllegalStateException("getResponse called on an empty response.")
    }

    companion object { // factory methods
        @JvmStatic
        fun notForTheBot(): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = false,
                isSpecialCommand = false,
                useMarkdown = false,
                answerIsFound = false,
                response = null
            )
        }

        @JvmStatic
        fun specialCommand(response: String?, useMarkdown: Boolean): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = true,
                isSpecialCommand = true,
                useMarkdown = useMarkdown,
                answerIsFound = true,
                response = response
            )
        }

        @JvmStatic
        fun answerFound(response: String): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = true,
                isSpecialCommand = false,
                useMarkdown = false,
                answerIsFound = true,
                response = response
            )
        }

        @JvmStatic
        fun answerNotFound(response: String?): MessageProcessingResult {
            return MessageProcessingResult(
                messageIsForTheBot = true,
                isSpecialCommand = false,
                useMarkdown = false,
                answerIsFound = false,
                response = response
            )
        }
    }
}
