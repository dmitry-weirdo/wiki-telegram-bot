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

        fun emptyChatUrl(chatString: String) = error(
            "Описание чата \"$chatString\": пустой URL чата."
        )

        fun noSeparatorInChatString(chatString: String) = error(
            "Описание чата \"$chatString\" не содержит ни разделителя \"${NotionCityChats.CHAT_LINK_AND_NAME_SEPARATOR_1}\", ни разделителя \"${NotionCityChats.CHAT_LINK_AND_NAME_SEPARATOR_2}\"."
        )

        fun chatUrlDoesNotStartWithHttps(chatString: String, url: String) = error(
            "Описание чата \"$chatString\": URL чата \"$url\" не начинается с \"${NotionCityChats.EXPECTED_URL_START}\"."
        )

        fun emptyChatName(chatString: String) = error(
            "Описание чата \"$chatString\": пустое имя чата. Проверьте, что чат существует, и добавьте его название в описание чата."
        )
    }
}
