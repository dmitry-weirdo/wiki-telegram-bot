package com.dv.telegram.notion

data class ChatParseResult(
    val chat: NotionCityChat?,
    val errorMessages: List<String>
) {
    val isEmpty: Boolean
        get() = !hasErrors() && (chat == null)

    val isCorrect: Boolean
        get() = !hasErrors() && (chat != null)

    fun hasErrors() = errorMessages.isNotEmpty()

    companion object {
        fun empty() = ChatParseResult(
            null,
            emptyList()
        )

        fun error(vararg errorMessages: String) = ChatParseResult(
            null,
            listOf(*errorMessages)
        )

        fun correctChat(url: String, name: String) = ChatParseResult(
            NotionCityChat(url, name),
            emptyList()
        )
    }
}
