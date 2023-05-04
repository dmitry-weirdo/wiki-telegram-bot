package com.dv.telegram.notion

import com.dv.telegram.util.WikiBotUtils
import notion.api.v1.NotionClient
import notion.api.v1.model.blocks.Block
import notion.api.v1.model.blocks.BlockType
import notion.api.v1.model.common.Emoji
import notion.api.v1.model.pages.PageParent
import notion.api.v1.model.pages.PageProperty
import org.apache.logging.log4j.kotlin.Logging
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object NotionPageTree : Logging {

    private const val LEVEL_SEPARATOR = "--"
    private const val NOTION_TOKEN_ENV_NAME = "NOTION_TOKEN"
    private const val PAGE_ID_ENV_NAME = "PAGE_ID"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss") // only primitives and Strings can be const o_O

    @JvmStatic
    fun main(args: Array<String>) {
        // todo: may also read from bot json config
        val notionToken = WikiBotUtils.getEnvVariable(NOTION_TOKEN_ENV_NAME)
        val pageId = WikiBotUtils.getEnvVariable(PAGE_ID_ENV_NAME)

        val tree = mutableListOf<String>()

        NotionPageUtils.execute(notionToken) { client ->
            // add
            val page = NotionPageUtils.retrievePage(client, pageId)
            val pageTitle = NotionPageUtils.getPageTitle(page)
            logger.info("")
            logger.info(pageTitle)

            tree.add(pageTitle)

            // add page children tree, NOT including the child pages
            parseTree(client, tree, pageId, 0)

            val treeToPrint = tree.joinToString("\n")
            logger.info("tree: \n$treeToPrint")

            createPage(client, pageId, "[❗ Beta ❗] Тестовый автоперевод главной страницы", treeToPrint)
        }
    }

    private fun parseTree(client: NotionClient, tree: MutableList<String>, blockId: String, currentLevel: Int) {
        val separator = LEVEL_SEPARATOR.repeat(currentLevel)
        val separatorNextLevel = LEVEL_SEPARATOR.repeat(currentLevel + 1)

        val children = try {
            client.retrieveBlockChildren(blockId)
        }
        catch (e: Exception) {
            logger.error("Error when getting children for block $blockId", e)
            logger.error("$separator ERROR GETTING CHILDREN FOR BLOCK $blockId")
            return
        }

        children.results.forEach {
            logger.info("")
            logger.info("$separator ${it.type} - ${it.id} - hasChildren: ${it.hasChildren}")

            tree.add("")
            tree.add("$separator ${it.type}")

            if (it.type == BlockType.HeadingOne) {
                val h1 = it.asHeadingOne()
                val richTexts = h1.heading1.richText

                printRichTexts(richTexts, separatorNextLevel, tree)
            }
            else if (it.type == BlockType.HeadingTwo) {
                val h2 = it.asHeadingTwo()
                val richTexts = h2.heading2.richText

                printRichTexts(richTexts, separatorNextLevel, tree)
            }
            else if (it.type == BlockType.HeadingThree) {
                val h3 = it.asHeadingThree()
                val richTexts = h3.heading3.richText

                printRichTexts(richTexts, separatorNextLevel, tree)
            }
            else if (it.type == BlockType.Paragraph) {
                val p = it.asParagraph()
                val richTexts = p.paragraph.richText

                printRichTexts(richTexts, separatorNextLevel, tree)
            }
            else if (it.type == BlockType.Callout) {
                val callout = it.asCallout()

                val icon = callout.callout?.icon
                logger.info("- $icon")

                val richTexts = callout.callout?.richText

                printRichTexts(richTexts, separatorNextLevel, tree)
            }
            else if (it.type == BlockType.BulletedListItem) {
                val bulletedListItem = it.asBulletedListItem()

                val richTexts = bulletedListItem.bulletedListItem.richText

                printRichTexts(richTexts, separatorNextLevel, tree)
            }
            else if (it.type == BlockType.Toggle) {
                val toggle = it.asToggle()

                val richTexts = toggle.toggle.richText

                printRichTexts(richTexts, separatorNextLevel, tree)
            }
            else if (it.type == BlockType.ChildPage) {
                // todo: exclude non-public child pages!!!

                val childPage = it.asChildPage()

                logger.info("$separatorNextLevel child page title: ${childPage.childPage.title}")

                tree.add("$separatorNextLevel ${childPage.childPage.title}")
            }
            else if (it.type == BlockType.Image) {
                val image = it.asImage()

                logger.info("$separatorNextLevel image type: ${image.image?.type}")

                tree.add("$separatorNextLevel ${image.image?.type}")
            }

            // todo: columnList
            // todo: synched blocks :scream:
            // todo: tables
            // todo: files?

            if (
                it.type != BlockType.ChildPage && // do NOT parse into the child pages
                it.hasChildren == true
            ) { // block has child blocks -> handle them at the next level
                parseTree(client, tree, it.id!!, currentLevel + 1)
            }
        }
    }

    private fun printRichTexts(richTexts: List<PageProperty.RichText>?, separator: String, tree: MutableList<String>) {
        richTexts?.forEach {
            // mention text (e.g. link on another page has plainText instead of text
            val hasPlainText = it.plainText?.isNotBlank() == true
            val hasContent = it.text?.content?.isNotBlank() == true

            if (hasPlainText) {
                logger.info("$separator ${it.plainText}")

                tree.add("$separator ${it.plainText}")
            }

            if (it.mention != null) {
                logger.info("$separator Mention type: ${it.mention?.type}")

                tree.add("$separator Mention type: ${it.mention?.type}")
            }

            if (hasContent) {
                logger.info("$separator ${it.text}")

                // links have both plainText and text.content -> do not add both
                // bulleted list items with page links have plain text only
                if (!hasPlainText) {
                    tree.add("$separator ${it.text}")
                }
            }
        }
    }

    private fun createPage(client: NotionClient, parentPageId: String, pageTitle: String, tree: String) {
        // todo: if child page with title exists, remove its children and just append the children

        // todo: probably move this page title hell to NotionPageUtils
        val title = PageProperty()
        title.title = listOf(
            PageProperty.RichText(
                text = PageProperty.RichText.Text(content = pageTitle)
            )
        )

        // todo: copy icon from the original page
        val icon = Emoji(emoji = "\uD83E\uDD16") // robot

        val parentPage = PageParent.page(parentPageId)

        val properties = mapOf(
            "title" to title
            // todo: other properties if required. Eg template type?
        )

        // paragraph with refresh pages
        val refreshTimeText = "Перевод страницы обновлён: ${ZonedDateTime.now().format(dateTimeFormatter)}"
        val refreshTimeParagraph = NotionPageUtils.createParagraph(refreshTimeText)

        // translation toggles
        val ruToggle = createToggle("Оригинальная страница (RU)", tree)
        val ruUkToggle = createTranslationToggle(tree, "ru", "uk") // see https://www.labnol.org/code/19899-google-translate-languages
        val ruDeToggle = createTranslationToggle(tree, "ru", "de") // see https://www.labnol.org/code/19899-google-translate-languages

        val pageBlocks = listOf(
            refreshTimeParagraph,
            ruToggle,
            ruUkToggle,
            ruDeToggle
        )

        val createdPage = client.createPage(
            parentPage,
            properties,
            pageBlocks,
            icon,
            null // todo: may also set cover or copy it from the original page
        )

        logger.info("Created page with title = \"$pageTitle\"\n$createdPage")
        logger.info("Created page id: ${createdPage.id}")
    }

    private fun createTranslationToggle(
        tree: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Block {
        val toggleHeader = "Перевод $sourceLanguage → $targetLanguage"

        val paragraphText = translate(tree, sourceLanguage, targetLanguage)

        return createToggle(toggleHeader, paragraphText)
    }

    private fun createToggle(
        toggleText: String,
        innerParagraphText: String
    ): Block {
        // todo: these hacks have to be solved, just save the original paragraph structure
        // paragraph can have up 2000 characters
        // children size is up to 100

        val maxParagraphCharsCount = 2000

        val separator = "\n\n"

        val splitByNewLines = innerParagraphText.split(separator)

        var paragraphText = ""

        val paragraphs = mutableListOf<Block>()

        for ((index, part) in splitByNewLines.withIndex()) {
            val next = if (index < splitByNewLines.size - 1) {
                splitByNewLines[index + 1]
            }
            else {
                ""
            }

            if (paragraphText.length + separator.length + next.length <= maxParagraphCharsCount) {
                paragraphText = paragraphText + separator + next
            }
            else {
                paragraphs.add(NotionPageUtils.createParagraph(paragraphText))
                paragraphText = ""
            }
        }

        if (paragraphText.isNotBlank()) { // add the last part
            paragraphs.add(NotionPageUtils.createParagraph(paragraphText))
        }

//        val paragraph = NotionPageUtils.createParagraph(innerParagraphText)

        return NotionPageUtils.createHeading1(
            toggleText,
            paragraphs
        )
    }

    private fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): String {
        // todo: translate with Google Translate
        return text + "\n\n$sourceLanguage → $targetLanguage"
    }
}
