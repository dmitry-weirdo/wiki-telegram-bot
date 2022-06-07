package com.dv.telegram.notion

import com.dv.telegram.data.CityChatData
import com.dv.telegram.exception.CommandException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class NotionCityChatsTest {

    @Test
    @DisplayName("Test parsing the chat string which should be in format \"https://chat.link - chat name\".")
    fun testParseChatString() {
        // empty string
        val emptyStringResult = NotionCityChats.parseChat("")
        assertThat(emptyStringResult).isEqualTo(ChatParseResult.empty())

        val blankStringResult = NotionCityChats.parseChat("  \t  ")
        assertThat(blankStringResult).isEqualTo(ChatParseResult.empty())

        // empty chat name
        val blankChatNameWithSeparator1 = "https://t.me/chat —   \t "
        val blankChatNameWithSeparator1Result = NotionCityChats.parseChat(blankChatNameWithSeparator1)
        assertThat(blankChatNameWithSeparator1Result).isEqualTo(
            ChatParseResult.emptyChatName(blankChatNameWithSeparator1)
        )

        val noChatNameWithSeparator1 = "https://t.me/chat — " // ends with separator -> split will give 1 string only
        val noChatNameWithSeparator1Result = NotionCityChats.parseChat(noChatNameWithSeparator1)
        assertThat(noChatNameWithSeparator1Result).isEqualTo(
            ChatParseResult.emptyChatName(noChatNameWithSeparator1)
        )

        val blankChatNameWithSeparator2 = "https://t.me/chat -  \t "
        val blankChatNameWithSeparator2Result = NotionCityChats.parseChat(blankChatNameWithSeparator2)
        assertThat(blankChatNameWithSeparator2Result).isEqualTo(
            ChatParseResult.emptyChatName(blankChatNameWithSeparator2)
        )

        val noChatNameWithSeparator2 = "https://t.me/chat - " // ends with separator -> split will give 1 string only
        val noChatNameWithSeparator2Result = NotionCityChats.parseChat(noChatNameWithSeparator2)
        assertThat(noChatNameWithSeparator2Result).isEqualTo(
            ChatParseResult.emptyChatName(noChatNameWithSeparator2)
        )

        // no separator
        val noSeparator = "https://t.me/chat Chat name but no separator"
        val noSeparatorResult = NotionCityChats.parseChat(noSeparator)
        assertThat(noSeparatorResult).isEqualTo(
            ChatParseResult.noSeparatorInChatString(noSeparator)
        )

        val noSeparatorChatUrlOnly = "https://t.me/chat"
        val noSeparatorChatUrlOnlyResult = NotionCityChats.parseChat(noSeparatorChatUrlOnly)
        assertThat(noSeparatorChatUrlOnlyResult).isEqualTo(
            ChatParseResult.noSeparatorInChatString(noSeparatorChatUrlOnly)
        )

        val noSeparatorChatNameOnly = "My best chat but it has no URL"
        val noSeparatorChatNameOnlyResult = NotionCityChats.parseChat(noSeparatorChatNameOnly)
        assertThat(noSeparatorChatNameOnlyResult).isEqualTo(
            ChatParseResult.noSeparatorInChatString(noSeparatorChatNameOnly)
        )

        // empty chat URL
        val noChatUrlWithSeparator1 = " \t  — Good chat name"
        val noChatUrlWithSeparator1Result = NotionCityChats.parseChat(noChatUrlWithSeparator1)
        assertThat(noChatUrlWithSeparator1Result).isEqualTo(
            ChatParseResult.emptyChatUrl(noChatUrlWithSeparator1)
        )

        val noChatUrlWithSeparator2 = " - Good chat name"
        val noChatUrlWithSeparator2Result = NotionCityChats.parseChat(noChatUrlWithSeparator2)
        assertThat(noChatUrlWithSeparator2Result).isEqualTo(
            ChatParseResult.emptyChatUrl(noChatUrlWithSeparator2)
        )

        // chat URL does not start with "https://"
        val chatUrlDoesNotStartWithHttps1 = "  http://t.me/chat_without_https  - Good chat name, but wrong protocol"
        val chatUrlDoesNotStartWithHttps1Result = NotionCityChats.parseChat(chatUrlDoesNotStartWithHttps1)
        assertThat(chatUrlDoesNotStartWithHttps1Result).isEqualTo(
            ChatParseResult.chatUrlDoesNotStartWithHttps(chatUrlDoesNotStartWithHttps1, "http://t.me/chat_without_https")
        )

        val chatUrlDoesNotStartWithHttps2 = " t.me/chat_without_https  - Good chat name, but wrong protocol"
        val chatUrlDoesNotStartWithHttps2Result = NotionCityChats.parseChat(chatUrlDoesNotStartWithHttps2)
        assertThat(chatUrlDoesNotStartWithHttps2Result).isEqualTo(
            ChatParseResult.chatUrlDoesNotStartWithHttps(chatUrlDoesNotStartWithHttps2, "t.me/chat_without_https")
        )

        val chatUrlDoesNotStartWithHttps3 = " Https://t.me/chat_without_https  - Good chat name, but wrong protocol" // case-sensitive "https://"
        val chatUrlDoesNotStartWithHttps3Result = NotionCityChats.parseChat(chatUrlDoesNotStartWithHttps3)
        assertThat(chatUrlDoesNotStartWithHttps3Result).isEqualTo(
            ChatParseResult.chatUrlDoesNotStartWithHttps(chatUrlDoesNotStartWithHttps3, "Https://t.me/chat_without_https")
        )

        // chat URL is only "https://"
        val chatUrlIsHttpsOnly = "  https://  - Chat with empty URL, http:// only"
        val chatUrlIsHttpsOnlyResult = NotionCityChats.parseChat(chatUrlIsHttpsOnly)
        assertThat(chatUrlIsHttpsOnlyResult).isEqualTo(
            ChatParseResult.emptyChatUrl(chatUrlIsHttpsOnly)
        )

        // correct chat with separator 1
        val correctChatWithSeparator1 = " \t https://t.me/good_chat \t  — \t Good chat name  \t  "
        val correctChatWithSeparator1Result = NotionCityChats.parseChat(correctChatWithSeparator1)
        assertThat(correctChatWithSeparator1Result).isEqualTo(
            ChatParseResult.correctChat("https://t.me/good_chat", "Good chat name") // expect to be trimmed
        )

        // correct chat with separator 2
        val correctChatWithSeparator2 = " \t https://t.me/good_chat \t  -  \t Good chat name  \t  "
        val correctChatWithSeparator2Result = NotionCityChats.parseChat(correctChatWithSeparator2)
        assertThat(correctChatWithSeparator2Result).isEqualTo(
            ChatParseResult.correctChat("https://t.me/good_chat", "Good chat name") // expect to be trimmed
        )
    }

    @Test
    @DisplayName("Test List<CityChatData> → List<NotionCityChats> conversion: errors, expect a thrown exception.")
    fun testFromWithErrors() {
        val cityChatsData = listOf(
            CityChatData(
                "   ", // empty city name -> will be just skipped
                "keywords",
                listOf("keywords"),
                listOf(
                    "bad",
                    "chats"
                )
            ),
            CityChatData(
                "Aachen",
                "Aachen",
                listOf("Aachen"),
                listOf( // empty chat names -> will be just skipped
                    "   ",
                    "",
                    "  \t  "
                )
            ),
            CityChatData(
                "Berlin", // good chat -> no errors
                "Berlin",
                listOf("Berlin"),
                listOf(
                    "https://t.me/berlin_chat - Berlin chat 1",
                    "https://t.me/berlin_work - Berlin work"
                )
            ),
            CityChatData(
                "Augsburg", // 2 of 3 chats have errors
                "Augsburg",
                listOf("Augsburg"),
                listOf(
                    "https://t.me/augsburg_chat - Augsburg good chat",
                    "https://t.me/augsburg_work - ", // no chat name
                    "http://t.me/augsburg_parents - Parents chats" // not https
                )
            ),
            CityChatData(
                "Duisburg", // 1 of 1 chats has errors
                "Duisburg",
                listOf("Duisburg"),
                listOf(
                    "https://t.me/duisburg_chat / Duisburg chat", // no correct separator
                )
            )
        )

        assertThatThrownBy { NotionCityChats.from(cityChatsData) }
            .isInstanceOf(CommandException::class.java)
            .extracting("errorMessages")
            .isEqualTo(listOf(
                ChatParseResult.emptyChatName("https://t.me/augsburg_work - ").errorMessages[0],
                ChatParseResult.chatUrlDoesNotStartWithHttps("http://t.me/augsburg_parents - Parents chats", "http://t.me/augsburg_parents").errorMessages[0],
                ChatParseResult.noSeparatorInChatString("https://t.me/duisburg_chat / Duisburg chat").errorMessages[0]
            ))
    }

    @Test
    @DisplayName("Test List<CityChatData> → List<NotionCityChats> conversion: successful conversion.")
    fun testFromWithoutErrors() {
        val cityChatsData = listOf(
            CityChatData(
                "   ", // empty city name -> will be just skipped
                "keywords",
                listOf("keywords"),
                listOf("bad", "chats")
            ),
            CityChatData(
                "Aachen",
                "Aachen",
                listOf("Aachen"),
                listOf( // empty chat names -> will be just skipped
                    "   ",
                    "",
                    "  \t  "
                )
            ),
            CityChatData(
                " Berlin  \t", // good chats
                "Berlin",
                listOf("Berlin"),
                listOf(
                    "  https://t.me/berlin_chat \t —  \t Berlin chat 1   ", // separator 1
                    "https://t.me/berlin_work - Berlin work", // separator 2
                    "  \t " // empty chat must be ignored
                )
            ),
            CityChatData(
                "Augsburg", // 1 of 1 chats is good
                "Augsburg",
                listOf("Augsburg"),
                listOf(
                    "https://t.me/augsburg_chat - Augsburg good chat"
                )
            ),
        )

        val chats = NotionCityChats.from(cityChatsData)
        assertThat(chats).hasSize(2) // only cities with at least 1 valid chat must be added

        // chats must be sorted alphabetically by cityName
        val city1 = chats[0]
        assertThat(city1.cityName).isEqualTo("Augsburg")
        assertThat(city1.chats).isEqualTo(listOf(
            NotionCityChat("https://t.me/augsburg_chat", "Augsburg good chat")
        ))

        val city2 = chats[1]
        assertThat(city2.cityName).isEqualTo("Berlin") // must be trimmed
        assertThat(city2.chats).isEqualTo(listOf(
            NotionCityChat("https://t.me/berlin_chat", "Berlin chat 1"),
            NotionCityChat("https://t.me/berlin_work", "Berlin work")
        ))

        val chatsCount = NotionCityChats.countTotalChats(chats)
        assertThat(chatsCount).isEqualTo(3)
    }
}
