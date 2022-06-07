package com.dv.telegram.notion

import org.assertj.core.api.Assertions.assertThat
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
        assertThat(blankChatNameWithSeparator1Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.emptyChatName(blankChatNameWithSeparator1)
        ))

        val noChatNameWithSeparator1 = "https://t.me/chat — " // ends with separator -> split will give 1 string only
        val noChatNameWithSeparator1Result = NotionCityChats.parseChat(noChatNameWithSeparator1)
        assertThat(noChatNameWithSeparator1Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.emptyChatName(noChatNameWithSeparator1)
        ))

        val blankChatNameWithSeparator2 = "https://t.me/chat -  \t "
        val blankChatNameWithSeparator2Result = NotionCityChats.parseChat(blankChatNameWithSeparator2)
        assertThat(blankChatNameWithSeparator2Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.emptyChatName(blankChatNameWithSeparator2)
        ))

        val noChatNameWithSeparator2 = "https://t.me/chat - " // ends with separator -> split will give 1 string only
        val noChatNameWithSeparator2Result = NotionCityChats.parseChat(noChatNameWithSeparator2)
        assertThat(noChatNameWithSeparator2Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.emptyChatName(noChatNameWithSeparator2)
        ))

        // no separator
        val noSeparator = "https://t.me/chat Chat name but no separator"
        val noSeparatorResult = NotionCityChats.parseChat(noSeparator)
        assertThat(noSeparatorResult).isEqualTo(ChatParseResult.error(
            NotionCityChats.noSeparatorInChatString(noSeparator)
        ))

        val noSeparatorChatUrlOnly = "https://t.me/chat"
        val noSeparatorChatUrlOnlyResult = NotionCityChats.parseChat(noSeparatorChatUrlOnly)
        assertThat(noSeparatorChatUrlOnlyResult).isEqualTo(ChatParseResult.error(
            NotionCityChats.noSeparatorInChatString(noSeparatorChatUrlOnly)
        ))

        val noSeparatorChatNameOnly = "My best chat but it has no URL"
        val noSeparatorChatNameOnlyResult = NotionCityChats.parseChat(noSeparatorChatNameOnly)
        assertThat(noSeparatorChatNameOnlyResult).isEqualTo(ChatParseResult.error(
            NotionCityChats.noSeparatorInChatString(noSeparatorChatNameOnly)
        ))

        // empty chat URL
        val noChatUrlWithSeparator1 = " \t  — Good chat name"
        val noChatUrlWithSeparator1Result = NotionCityChats.parseChat(noChatUrlWithSeparator1)
        assertThat(noChatUrlWithSeparator1Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.emptyChatUrl(noChatUrlWithSeparator1)
        ))

        val noChatUrlWithSeparator2 = " - Good chat name"
        val noChatUrlWithSeparator2Result = NotionCityChats.parseChat(noChatUrlWithSeparator2)
        assertThat(noChatUrlWithSeparator2Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.emptyChatUrl(noChatUrlWithSeparator2)
        ))

        // chat URL does not start with "https://"
        val chatUrlDoesNotStartWithHttps1 = "  http://t.me/chat_without_https  - Good chat name, but wrong protocol"
        val chatUrlDoesNotStartWithHttps1Result = NotionCityChats.parseChat(chatUrlDoesNotStartWithHttps1)
        assertThat(chatUrlDoesNotStartWithHttps1Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.chatUrlDoesNotStartWithHttps(chatUrlDoesNotStartWithHttps1, "http://t.me/chat_without_https")
        ))

        val chatUrlDoesNotStartWithHttps2 = " t.me/chat_without_https  - Good chat name, but wrong protocol"
        val chatUrlDoesNotStartWithHttps2Result = NotionCityChats.parseChat(chatUrlDoesNotStartWithHttps2)
        assertThat(chatUrlDoesNotStartWithHttps2Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.chatUrlDoesNotStartWithHttps(chatUrlDoesNotStartWithHttps2, "t.me/chat_without_https")
        ))

        val chatUrlDoesNotStartWithHttps3 = " Https://t.me/chat_without_https  - Good chat name, but wrong protocol" // case-sensitive "https://"
        val chatUrlDoesNotStartWithHttps3Result = NotionCityChats.parseChat(chatUrlDoesNotStartWithHttps3)
        assertThat(chatUrlDoesNotStartWithHttps3Result).isEqualTo(ChatParseResult.error(
            NotionCityChats.chatUrlDoesNotStartWithHttps(chatUrlDoesNotStartWithHttps3, "Https://t.me/chat_without_https")
        ))

        // chat URL is only "https://"
        val chatUrlIsHttpsOnly = "  https://  - Chat with empty URL, http:// only"
        val chatUrlIsHttpsOnlyResult = NotionCityChats.parseChat(chatUrlIsHttpsOnly)
        assertThat(chatUrlIsHttpsOnlyResult).isEqualTo(ChatParseResult.error(
            NotionCityChats.emptyChatUrl(chatUrlIsHttpsOnly)
        ))

        // correct chat with separator 1
        val correctChatWithSeparator1 = " \t https://t.me/good_chat \t  — \t Good chat name  \t  "
        val correctChatWithSeparator1Result = NotionCityChats.parseChat(correctChatWithSeparator1)
        assertThat(correctChatWithSeparator1Result).isEqualTo(ChatParseResult.correctChat(
            "https://t.me/good_chat", "Good chat name" // expect to be trimmed
        ))

        // correct chat with separator 2
        val correctChatWithSeparator2 = " \t https://t.me/good_chat \t  -  \t Good chat name  \t  "
        val correctChatWithSeparator2Result = NotionCityChats.parseChat(correctChatWithSeparator2)
        assertThat(correctChatWithSeparator2Result).isEqualTo(ChatParseResult.correctChat(
            "https://t.me/good_chat", "Good chat name" // expect to be trimmed
        ))
    }
}
