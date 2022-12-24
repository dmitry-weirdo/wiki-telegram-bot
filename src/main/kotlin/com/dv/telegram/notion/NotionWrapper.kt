package com.dv.telegram.notion

import com.dv.telegram.GoogleSheetLoader
import com.dv.telegram.data.ChatData
import com.dv.telegram.exception.CommandException
import com.dv.telegram.tabs.TabType
import com.dv.telegram.util.WikiBotUtils
import notion.api.v1.NotionClient
import notion.api.v1.model.blocks.Block
import notion.api.v1.model.blocks.HeadingOneBlock
import notion.api.v1.model.blocks.ParagraphBlock
import org.apache.logging.log4j.kotlin.Logging
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object NotionWrapper : Logging {
    private const val NOTION_TOKEN_ENV_NAME = "NOTION_TOKEN"

    private const val TOGGLE_HEADER_1_TO_APPEND_TEXT = "Чаты по землям и городам Германии (Telegram, WhatsApp)"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss") // only primitives and Strings can be const o_O

    @JvmStatic
    fun main(args: Array<String>) {
/*        val wikiBotConfigs = WikiBotUtils.readConfigs()

        val threadsCount = wikiBotConfigs.configs.size
        logger.info("Total bot configs: $threadsCount")

        val config = wikiBotConfigs.configs[0]
        val botData = GoogleSheetLoader.readGoogleSheetTabs(config)

        val cityChatsTab = botData
            .getCityChats()
            ?: throw CommandException("В конфиге бота нет вкладок с типом ${TabType.CITY_CHATS}.")

        val cityChatsData = cityChatsTab.answers as List<ChatData>
        val cityChats = NotionCityChats.from(cityChatsData)
//        val cityChats = getCityChats(); // use test data

        logger.info("$cityChats.size city chats read from Google Sheet.")*/

        val notionToken = WikiBotUtils.getEnvVariable(NOTION_TOKEN_ENV_NAME)

//        val pageId = NotionPageIds.MAIN_PAGE // Помощь украинцам в Германии
        val pageId = NotionPageIds.NOTION_API_TEST_PAGE // Test page for Notion API
//        val pageId = NotionPageIds.GERMAN_CITY_CHATS_RU // список чатов по городам (german-city-chats)
//        val pageId = "67sadfsadfjlkfdsaj" // incorrect page

        if (true) {
            appendPageMention(notionToken, pageId)
            return
        }


/*
        if (true) {
            appendToggleHeadingOne(notionToken, pageId);
            return;
        }
*/

/*
        appendCityChats(
            config.getNotionToken(),
            config.getCityChatsPageId(),
            config.getCityChatsToggleHeading1Text(),
            cityChats
        );
*/

/*
        appendCityChats(
            notionToken,
            pageId,
            TOGGLE_HEADER_1_TO_APPEND_TEXT,
            cityChats
        )
*/
    }

    private fun appendPageMention(notionToken: String, pageId: String) {

        NotionPageUtils.execute(notionToken) {
            val textBeforeLink = NotionPageUtils.createRichText("This will be a mention link 3 — ")

            val mentionedPageId = NotionPageIds.MAIN_PAGE
            val mentionRichText = NotionPageUtils.createRichTextWithPageMention(it, mentionedPageId)

            val paragraph = ParagraphBlock(
                ParagraphBlock.Element(
                    listOf(textBeforeLink, mentionRichText)
                )
            )

            it.appendBlockChildren(pageId, listOf(paragraph))
        }
    }

    private fun appendToggleHeadingOne(notionToken: String, pageId: String) {
        NotionPageUtils.execute(notionToken) { appendToggleHeadingOne(it, pageId) }
    }

    private fun appendToggleHeadingOne(client: NotionClient, pageId: String) {
        val page = NotionPageUtils.retrievePage(client, pageId)

        val paragraphInHeader = NotionPageUtils.createParagraph("Paragraph as heading 1 child")

        val heading1Element = HeadingOneBlock.Element(
            richText = NotionPageUtils.createRichTextList("Heading 1 created with Notion API, hasChildren = true try"),
            children = listOf(paragraphInHeader) // heading with children is created as Toggle Heading
        )

        val heading1: Block = HeadingOneBlock(heading1Element)

        NotionPageUtils.append(client, pageId, heading1)
    }

    fun appendCityChats(
        notionToken: String,
        pageId: String,
        toggleHeading1Text: String,
        cityChats: List<NotionCityChats>
    ): NotionCityChatsImportResult {
        NotionOperationBlocker.startOperation()

        var result: NotionCityChatsImportResult? = null

        NotionPageUtils.execute(notionToken) { client ->
            result = appendCityChats(client, pageId, toggleHeading1Text, cityChats)
        }

        NotionOperationBlocker.stopOperation()

        return result!!
    }

    private fun appendCityChats(
        client: NotionClient,
        pageId: String,
        toggleHeading1Text: String,
        cityChats: List<NotionCityChats>
    ): NotionCityChatsImportResult {
        val page = NotionPageUtils.retrievePage(client, pageId)
        val pageTitle = NotionPageUtils.getPageTitle(page)

        logger.info("Page with id = $pageId successfully retrieved.")
        logger.info("Page url: ${page.url}")
        logger.info("Page created by: ${page.createdBy.name}")
        logger.info("Page title: $pageTitle")

        val blocks = client.retrieveBlockChildren(pageId, null, 100)
        logger.info("Total blocks retrieved from the page: ${blocks.results.size}")

        val rootBlock = NotionPageUtils.deleteToggleHeading1Content(client, blocks, toggleHeading1Text)

        // append paragraph with refresh time
        val refreshTimeText = "Список чатов обновлён: ${ZonedDateTime.now().format(dateTimeFormatter)}"
        val refreshTimeParagraph = NotionPageUtils.createParagraph(refreshTimeText)
        NotionPageUtils.append(client, rootBlock, refreshTimeParagraph)

        // append paragraph with total cities count
        val totalCities = cityChats.size
        val totalCitiesText = "Всего городов: $totalCities"
        val totalCitiesParagraph = NotionPageUtils.createParagraph(totalCitiesText)
        NotionPageUtils.append(client, rootBlock, totalCitiesParagraph)

        // append paragraph with total cities count
        val totalChats = NotionCityChats.countTotalChats(cityChats)
        val totalChatsText = "Всего чатов: $totalChats"
        val totalChatsParagraph = NotionPageUtils.createParagraph(totalChatsText)
        NotionPageUtils.append(client, rootBlock, totalChatsParagraph)

        // append toggles with city chats
        val cityChatToggles = NotionPageUtils.getCityChatToggles(cityChats)

        // 13.09.2022: Notion API started to fail when adding more than 100 elements in one operation -> split to list of 100 and append chunk by chunk
        val cityChatTogglesChunked = cityChatToggles.chunked(100)

        cityChatTogglesChunked.forEach {
            NotionPageUtils.append(client, rootBlock, it)
        }

        logger.info("$totalChats chats for $totalCities cities appended to Notion page $pageId (\"$pageTitle\"), toggle header 1 \"$toggleHeading1Text\".")

        return NotionCityChatsImportResult(
            pageId,
            pageTitle,
            toggleHeading1Text,
            totalCities,
            totalChats
        )
    }

    private fun getTestCityChats(): List<NotionCityChats> { // get test data
        // city 1
        val city1 = NotionCityChats("Ansbach")

        city1.addChat(
            "https://t.me/+QQ9lx56QjYU1ZjZi",
            "Ansbach/Landkreis Ansbach \uD83C\uDDE9\uD83C\uDDEA/Ukraine \uD83C\uDDFA\uD83C\uDDE6" // test with emojis
        )

        // city 2
        val city2 = NotionCityChats("Bottrop")

        city2.addChat(
            "https://t.me/+lWmTWIFgAI9iN2Qy",
            "Помощь Украинцам \uD83C\uDDFA\uD83C\uDDE6Bottrop\uD83C\uDDE9\uD83C\uDDEA Кто знает что?"
        )

        city2.addChat(
            "https://t.me/uahelp_ruhrgebiet",
            "UA Help Ruhrgebiet"
        )

        return listOf(city1, city2)
            .sortedBy { it.cityName }
    }
}
