package com.dv.telegram.notion

import com.dv.telegram.data.ChatData
import com.dv.telegram.exception.CommandException
import org.apache.logging.log4j.kotlin.Logging

data class NotionCityChats (
    val cityName: String?,
    val chats: MutableList<NotionCityChat> = mutableListOf()
) {
    fun addChat(chat: NotionCityChat) {
        chats.add(chat)
    }

    fun addChat(url: String, name: String) {
        addChat(
            NotionCityChat(url, name)
        )
    }

    companion object : Logging { // see https://www.baeldung.com/kotlin/logging#6-combining-with-companion-objects
        const val CHAT_LINK_AND_NAME_SEPARATOR_1 = " — "
        const val CHAT_LINK_AND_NAME_SEPARATOR_2 = " - "
        const val EXPECTED_URL_START = "https://"

        fun countTotalChats(cityChats: List<NotionCityChats>) = cityChats.sumOf { it.chats.size }

        fun from(cityChatsData: List<ChatData>): List<NotionCityChats> {
            val errors: MutableList<String> = ArrayList()

            val chats = cityChatsData.mapNotNull {
                from(it, errors)
            } // filter out empty lines (without city name) ( map -> filterNotNull )

            if (errors.isNotEmpty()) {
                throw CommandException(errors)
            }

            // sort cities by name
            return chats
                .filter { it.chats.isNotEmpty() } // do not add cities without chats
                .sortedBy { it.cityName }
        }

        fun from(cityChatData: ChatData, errors: MutableList<String>): NotionCityChats? {
            val cityName = cityChatData.chatLabel.trim()
            if (cityName.isBlank()) {
                // todo: error on empty city name + non-empty chats?
                logger.warn("Empty city name.")
                return null
            }

            val chats = NotionCityChats(cityName)

            for (chatUrlAndName in cityChatData.chats) {
                val chatParseResult = parseChat(chatUrlAndName)

                if (chatParseResult.isEmpty) { // no chat and no error -> just skip
                    continue
                }

                if (chatParseResult.hasErrors()) {
                    errors.addAll(chatParseResult.errorMessages)
                }

                if (chatParseResult.chat != null) {
                    chats.addChat(chatParseResult.chat)
                }
            }

            return chats
        }

        fun parseChat(chatString: String): ChatParseResult {
            if (chatString.isBlank()) {
                return ChatParseResult.empty()
            }

            val split: List<String>

            if (chatString.contains(CHAT_LINK_AND_NAME_SEPARATOR_1)) {
                split = chatString.split(CHAT_LINK_AND_NAME_SEPARATOR_1)

                if (split.size < 2) {
                    return ChatParseResult.emptyChatName(chatString)
                }
            }
            else if (chatString.contains(CHAT_LINK_AND_NAME_SEPARATOR_2)) {
                split = chatString.split(CHAT_LINK_AND_NAME_SEPARATOR_2)

                if (split.size < 2) {
                    return ChatParseResult.emptyChatName(chatString)
                }
            }
            else {
                logger.warn(
                    "City chat string \"$chatString\" does not contain neither separator \"$CHAT_LINK_AND_NAME_SEPARATOR_1\" nor separator \"$CHAT_LINK_AND_NAME_SEPARATOR_2\"."
                )

                return ChatParseResult.noSeparatorInChatString(chatString)
            }

            val url = split[0].trim()
            val name = split[1].trim()

            if (url.isBlank()) {
                return ChatParseResult.emptyChatUrl(chatString)
            }

            if (!url.startsWith(EXPECTED_URL_START)) {
                logger.warn("Chat URL \"$url\" does not start with \"$EXPECTED_URL_START\".")

                return ChatParseResult.chatUrlDoesNotStartWithHttps(chatString, url)
            }

            if (url == EXPECTED_URL_START) {
                logger.warn("Chat URL is only \"$EXPECTED_URL_START\".")

                return ChatParseResult.emptyChatUrl(chatString)
            }

            // todo: probably check the URL format correctness
            // todo: in the best case, go to URL and check that it exists

            if (name.isBlank()) {
                logger.warn("Chat name for chat with url \"$url\" is empty. Please check that the chat exists and add its name.")

                return ChatParseResult.emptyChatName(chatString)
            }

            return ChatParseResult.correctChat(url, name)
        }
    }
}
