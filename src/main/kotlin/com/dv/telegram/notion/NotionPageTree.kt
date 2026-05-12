package com.dv.telegram.notion

import com.dv.telegram.excel.PageTreeXlsxWriter
import com.dv.telegram.google.GoogleCloudTranslateApp
import com.dv.telegram.util.WikiBotUtils
import notion.api.v1.NotionClient
import notion.api.v1.model.blocks.Block
import notion.api.v1.model.blocks.BlockType
import notion.api.v1.model.blocks.ParagraphBlock
import notion.api.v1.model.common.Emoji
import notion.api.v1.model.pages.Page
import notion.api.v1.model.pages.PageParent
import notion.api.v1.model.pages.PageProperty
import org.apache.logging.log4j.kotlin.Logging
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object NotionPageTree : Logging {

    private const val LEVEL_SEPARATOR = "--"
    private const val NOTION_TOKEN_ENV_NAME = "NOTION_TOKEN"
    private const val PAGE_ID_ENV_NAME = "PAGE_ID"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss") // only primitives and Strings can be const o_O

    @JvmStatic
    fun main(args: Array<String>) {
        val fakeTree = createFakePageTreeForTests()
        exportRowsToExcelFile(fakeTree)

        if (true) {
            return
        }

        // todo: may also read from bot json config
        val notionToken = WikiBotUtils.getEnvVariable(NOTION_TOKEN_ENV_NAME)
//        val pageId = WikiBotUtils.getEnvVariable(PAGE_ID_ENV_NAME)
//        val pageId = NotionPageIds.FIRST_STEPS_RU
        val pageId = NotionPageIds.MAIN_PAGE

        val tree = mutableListOf<String>()

        NotionPageUtils.execute(notionToken) { client ->
            // add
            val page = NotionPageUtils.retrievePage(client, pageId)
            val pageTitle = NotionPageUtils.getPageTitle(page)
            logger.info("")
            logger.info(pageTitle)

            tree.add(pageTitle)

            val node = NotionPageNode(
                NotionPageNode.ROOT_LEVEL,
                pageId,
                "",
                pageTitle,
                NotionPageUtils.parseNotionDateTimeString(page.lastEditedTime),
                null
            )

            // add page children tree, NOT including the child pages
//            parseTree(client, tree, pageId, 0)

            // find the child pages tree
            val startCollect = System.currentTimeMillis()

            NotionPageTreeCollector.collectPagesTree(client, tree, node, pageId, node.level)

            val endCollect = System.currentTimeMillis()
            val collectTime = formatDuration(endCollect - startCollect)

            logger.info("Collecting pages finished. Total root nodes collected: ${node.children.size}. Time spent: $collectTime")

            // Toggle heading block within "All pages tree" page
            // https://www.notion.so/russiansabroad/All-pages-tree-7f50480fb5614e11b9e379b19b311c2a?source=copy_link#35abc3d74a3080a48dc3e1b284f7d6b6
            // https://www.notion.so/russiansabroad/All-pages-tree-7f50480fb5614e11b9e379b19b311c2a?source=copy_link#35abc3d74a3080a48dc3e1b284f7d6b6

            // This is the id of the first parent block re
            // 4807b405-2850-4d6f-ac89-38de5752c730
            // https://www.notion.so/russiansabroad/All-pages-tree-7f50480fb5614e11b9e379b19b311c2a?source=copy_link#4807b40528504d6fac8938de5752c730

            // 08.May.2026 toggle
//            val toggleHeadingBlockId = "35abc3d74a3080a48dc3e1b284f7d6b6"

            // 09.May.2026 toggle - Run 1
//            val toggleHeadingBlockId = "35bbc3d74a308006bea9f4aa5fa62383"

            // 09.May.2026 toggle - Run 2
            val toggleHeadingBlockId = "35bbc3d74a3080af8159faa650ca3eba"
            // https://www.notion.so/russiansabroad/All-pages-tree-7f50480fb5614e11b9e379b19b311c2a?source=copy_link#35bbc3d74a3080af8159faa650ca3eba


//            val pageToAppend = NotionPageUtils.retrievePage(client, NotionPageIds.NOTION_API_TEST_PAGE)
//            appendTree(client, NotionPageIds.NOTION_API_TEST_PAGE, node, 0)
//            appendTree(client, NotionPageIds.ALL_PAGES_TREE, node, 0)
//            appendTree(client, NotionPageIds.ALL_PAGES_TREE, node, 0)

            val blockToAppendTo = toggleHeadingBlockId

            try {
                val startAppend = System.currentTimeMillis()

                appendTree(client, blockToAppendTo, node, 0)

                val endAppend = System.currentTimeMillis()
                val appendTime = formatDuration(endAppend - startAppend)
                logger.info("Appending tree finished. Time spent: $appendTime")
            }
            catch (e: Exception) {
                logger.error("Error while appending tree to block \"$blockToAppendTo\"", e)
            }

            // export to Excel even if exporting the tree into Notion page has failed
            val startExport = System.currentTimeMillis()

            exportRowsToExcelFile(node)

            val endExport = System.currentTimeMillis()
            val exportTime = formatDuration(endExport - startExport)
            logger.info("Exporting to Excel finished. Time spent: $exportTime")

            val treeToPrint = tree.joinToString("\n")
            logger.info("tree: \n$treeToPrint")

//            createPage(client, pageId, "[❗ Beta ❗] Тестовый автоперевод главной страницы", treeToPrint)
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d (minutes:seconds)", minutes, seconds)
    }

    /**
     * Small in-memory tree for tests or local experiments (no Notion API): root (level 0) → two nodes (level 1) → one child each (level 2).
     * Page ids are 32-char hex strings like the API; [NotionPageNode.parent] / [NotionPageNode.children] match [NotionPageNode.parentId] / levels.
     */
    fun createFakePageTreeForTests(): NotionPageNode {
        val root = NotionPageNode(
            NotionPageNode.ROOT_LEVEL,
            "10000000000000000000000000000001",
            "",
            "Fake tree — root",
            ZonedDateTime.of(2024, 6, 1, 9, 0, 0, 0, ZoneOffset.UTC),
            null
        )

        val childA = NotionPageNode(
            1,
            "20000000000000000000000000000002",
            root.id,
            "Fake tree — level 1 — A",
            ZonedDateTime.of(2024, 6, 2, 10, 30, 0, 0, ZoneOffset.UTC),
            root
        )
        root.children.add(childA)

        val grandA = NotionPageNode(
            2,
            "30000000000000000000000000000003",
            childA.id,
            "Fake tree — level 2 — under A",
            ZonedDateTime.of(2024, 6, 3, 12, 15, 45, 0, ZoneOffset.UTC),
            childA
        )
        childA.children.add(grandA)

        val childB = NotionPageNode(
            1,
            "20000000000000000000000000000004",
            root.id,
            "Fake tree — level 1 — B",
            ZonedDateTime.of(2024, 7, 1, 8, 0, 0, 0, ZoneOffset.UTC),
            root
        )
        root.children.add(childB)

        val grandB = NotionPageNode(
            2,
            "30000000000000000000000000000005",
            childB.id,
            "Fake tree — level 2 — under B",
            ZonedDateTime.of(2024, 7, 2, 16, 45, 30, 0, ZoneOffset.UTC),
            childB
        )
        childB.children.add(grandB)

        return root
    }

    private fun appendTree(client: NotionClient, parentBlockId: String, node: NotionPageNode, level: Int) {
//        val page = NotionPageUtils.retrievePage(client, node.id) // retrieve the complete Page object is required for page mention

        val page = retryRetrievePage(
            client = client,
            pageId = node.id,
            maxRetries = 10,
            waitMillis = 1000L
        )

        val separatorText = node.getSeparator()

        val separatorBeforeLink = NotionPageUtils.createRichText(separatorText)

//        val lastEditTime = NotionPageUtils.parseNotionDateTimeString(page.lastEditedTime)
//        val lastEditTimeString = lastEditTime.format(dateTimeFormatter)
        val lastEditTimeString = node.lastEditedTime.format(dateTimeFormatter)

        val lastEditText = NotionPageUtils.createRichText("  (edited: $lastEditTimeString)")

        val mentionRichText = NotionPageUtils.createRichTextWithPageMention(page)

        val paragraph = ParagraphBlock(
            ParagraphBlock.Element(
                listOf(
                    separatorBeforeLink,
                    mentionRichText,
                    lastEditText
                )
            )
        )

        client.appendBlockChildren(parentBlockId, listOf(paragraph))

        for (child in node.children) {
            appendTree(client, parentBlockId, child, level + 1)
        }
    }

    private fun retryRetrievePage(
        client: NotionClient,
        pageId: String,
        maxRetries: Int,
        waitMillis: Long
    ): Page {
        var attempt = 0

        while (attempt < maxRetries) {
            try {
                return NotionPageUtils.retrievePage(client, pageId)
            }
            catch (e: Exception) {
                logger.error("Error when getting retrieving page $pageId, attempt $attempt / $maxRetries.", e)

                attempt++

                if (attempt >= maxRetries) {
                    throw e
                }

                Thread.sleep(waitMillis)
            }
        }

        throw RuntimeException("Failed to retrieve page after $maxRetries attempts")
    }

    private fun exportRowsToExcelFile(root: NotionPageNode) {
        val nodes = mutableListOf<NotionPageNode>()

        fillRowsForExcelFile(root, nodes)
        logger.info("Total page rows: ${nodes.size}")

        val directory = "c:/java/wiki-telegram-bot"
        val filePath = PageTreeXlsxWriter.getFilePath(directory, root.id, ZonedDateTime.now())

        PageTreeXlsxWriter.createXlsxFile(filePath, nodes)
    }

    private fun fillRowsForExcelFile(node: NotionPageNode, nodes: MutableList<NotionPageNode>) {
        nodes.add(node)

        for (child in node.children) {
            fillRowsForExcelFile(child, nodes)
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
            // todo: synced blocks :scream:
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
        val ruToggle = createToggle("Оригинальная страница (${GoogleCloudTranslateApp.LANGUAGE_RUSSIAN})", tree)
        val ruUkToggle = createTranslationToggle(tree, GoogleCloudTranslateApp.LANGUAGE_RUSSIAN, GoogleCloudTranslateApp.LANGUAGE_UKRAINIAN)
        val ruDeToggle = createTranslationToggle(tree, GoogleCloudTranslateApp.LANGUAGE_RUSSIAN, GoogleCloudTranslateApp.LANGUAGE_GERMAN)

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

        // todo: move to constant in NotionPageUtils
        val maxParagraphCharsCount = 2000

        val separator = "\n\n"

        val splitByNewLines = innerParagraphText.split(separator)

        var paragraphText = splitByNewLines[0] // todo: check that there is at least 1 element

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
        return GoogleCloudTranslateApp.translateText(sourceLanguage, targetLanguage, text)
//        return text + "\n\n$sourceLanguage → $targetLanguage"
    }
}
