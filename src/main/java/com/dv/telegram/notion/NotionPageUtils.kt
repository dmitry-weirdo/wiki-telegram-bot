package com.dv.telegram.notion

import com.dv.telegram.exception.CommandException
import notion.api.v1.NotionClient
import notion.api.v1.http.OkHttp4Client
import notion.api.v1.model.blocks.*
import notion.api.v1.model.common.RichTextLinkType
import notion.api.v1.model.common.RichTextType
import notion.api.v1.model.pages.Page
import notion.api.v1.model.pages.PageProperty.RichText
import org.apache.logging.log4j.kotlin.Logging

object NotionPageUtils : Logging {

    private const val MAX_PAGE_SIZE = 100 // setting more also returns 100

    private const val CHAT_LINK_AND_NAME_SEPARATOR = " — "

    private const val CONNECT_TIMEOUT_MILLISECONDS = 60000 // 1 minute = 60 seconds = 60000 milliseconds
    private const val WRITE_TIMEOUT_MILLISECONDS = 60000 // 1 minute = 60 seconds = 60000 milliseconds
    private const val READ_TIMEOUT_MILLISECONDS = 60000 // 1 minute = 60 seconds = 60000 milliseconds

    fun execute(
        notionToken: String,
        task: (client: NotionClient) -> Unit
    ) {
        NotionClient(notionToken).use { client -> // use is a try-with-resource
            setHttpClient(client)

            task(client)
        }
    }

    private fun setHttpClient(client: NotionClient) {
        // increase timeouts since writing a lot of toggles at once can lead to connection timeout
        val httpClient = OkHttp4Client(
            CONNECT_TIMEOUT_MILLISECONDS,
            WRITE_TIMEOUT_MILLISECONDS,
            READ_TIMEOUT_MILLISECONDS
        )

        client.httpClient = httpClient
    }

    fun retrievePage(client: NotionClient, pageId: String): Page {
        return try {
            client.retrievePage(pageId)
        }
        catch (e: Exception) {
            throw CommandException("Ошибка при получении страницы Notion с pageId = \"$pageId\".", e)
        }
    }

    fun getPageTitle(page: Page): String {
        return page
            .properties["title"]
            ?.title
            ?.get(0)
            ?.text
            ?.content
            ?: throw CommandException("Не удалось получить название страницы ${page.id}.")
    }

    fun deleteToggleHeading1Content(client: NotionClient, blocks: Blocks, heading1Text: String): HeadingOneBlock {
        val heading1ToAppend = getToggleHeading1Content(blocks, heading1Text)

        var deletedBlocksCount = 0
        var blockChildren = client.retrieveBlockChildren(
            heading1ToAppend.id!!,
            null,
            MAX_PAGE_SIZE // max pageSize is 100
        )

        while (blockChildren.results.isNotEmpty()) {
            for (childBlock in blockChildren.results) {
                client.deleteBlock(childBlock.id!!)
            }

            deletedBlocksCount += blockChildren.results.size

            blockChildren = client.retrieveBlockChildren(
                heading1ToAppend.id!!,
                null,
                MAX_PAGE_SIZE // max pageSize is 100
            )
        }

        logger.info("Removed $deletedBlocksCount child blocks from heading one with text \"$heading1Text\".")

        return heading1ToAppend
    }

    private fun getToggleHeading1Content(blocks: Blocks, heading1Text: String): HeadingOneBlock {
        val headersWithText = blocks
            .results
            .filter {
                (it.type == BlockType.HeadingOne)
                    && headingHasText(it.asHeadingOne(), heading1Text)
            }

        if (headersWithText.isEmpty()) {
            throw CommandException("Header 1 с текстом \"$heading1Text\" не найден.")
        }

        if (headersWithText.size > 1) {
            throw CommandException("Найден более чем один header 1 с текстом \"$heading1Text\". Всего заголовков: ${headersWithText.size}.")
        }

        return headersWithText[0].asHeadingOne()
    }

    private fun headingHasText(heading1: HeadingOneBlock, text: String): Boolean {
        val richTexts: List<RichText> = heading1.heading1.richText

        if (richTexts.isEmpty()) {
            return false
        }

        val richText: RichText.Text = richTexts[0].text
            ?: return false

        val content = richText.content
        if (content.isNullOrBlank()) {
            return false
        }

        return content == text
    }

    // RichText methods
    fun createRichTextList(text: String) = listOf(
        createRichText(text)
    )

    fun createRichText(text: String) = RichText(
        RichTextType.Text,
        RichText.Text(text)
    )

    fun createRichTextLink(text: String?, url: String?): RichText {
        val link = RichText.Link(RichTextLinkType.Url, url)

        return RichText(
            RichTextType.Text,
            RichText.Text(text, link)
        )
    }

    // ParagraphBlock methods
    fun createParagraph(text: String, children: List<Block> = listOf()) = ParagraphBlock(
        ParagraphBlock.Element(
            createRichTextList(text),
            children
        )
    )

    // ToggleBlock methods
    fun getCityChatToggles(cityChats: List<NotionCityChats>): List<ToggleBlock> {
        return cityChats
            .map { createToggle(it) }
    }

    fun createToggle(city: NotionCityChats): ToggleBlock {
        val chats = city.chats

        val bullets = chats
            .map { createBulletWithChatLink(it) }

        return createToggle(
            city.cityName!!,
            bullets
        )
    }

    fun createToggle(text: String, children: List<Block> = listOf()) = ToggleBlock(
        ToggleBlock.Element(
            createRichTextList(text),
            children
        )
    )

    // BulletedListItemBlock methods
    fun createBullet(text: String, children: List<Block> = listOf()) = BulletedListItemBlock(
        BulletedListItemBlock.Element(
            createRichTextList(text),
            children
        )
    )

    fun createBulletWithChatLink(chat: NotionCityChat) = createBulletWithChatLink(chat.url, chat.name)

    fun createBulletWithChatLink(chatLink: String, chatName: String) = BulletedListItemBlock(
        BulletedListItemBlock.Element(
            listOf(
                createRichTextLink(chatLink, chatLink),
                createRichText("$CHAT_LINK_AND_NAME_SEPARATOR$chatName")
            )
        )
    )
}
