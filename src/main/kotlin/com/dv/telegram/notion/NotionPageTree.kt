package com.dv.telegram.notion

import com.dv.telegram.util.WikiBotUtils
import notion.api.v1.NotionClient
import notion.api.v1.model.blocks.BlockType
import notion.api.v1.model.pages.PageProperty
import org.apache.logging.log4j.kotlin.Logging

object NotionPageTree : Logging {

    private const val LEVEL_SEPARATOR = "--"
    private const val NOTION_TOKEN_ENV_NAME = "NOTION_TOKEN"
    private const val PAGE_ID_ENV_NAME = "PAGE_ID"

    @JvmStatic
    fun main(args: Array<String>) {
        // todo: may also read from bot json config
        val notionToken = WikiBotUtils.getEnvVariable(NOTION_TOKEN_ENV_NAME)
        val pageId = WikiBotUtils.getEnvVariable(PAGE_ID_ENV_NAME)

        val tree = mutableListOf<String>()

        NotionPageUtils.execute(notionToken) { client ->
            parseTree(client, tree, pageId, 0)
        }

        val treeToPrint = tree.joinToString("\n")
        logger.info("tree: \n$treeToPrint")
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
            return;
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
                logger.info("- ${icon}")

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
}