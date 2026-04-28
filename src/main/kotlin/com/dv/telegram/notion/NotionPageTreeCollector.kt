package com.dv.telegram.notion

import notion.api.v1.NotionClient
import notion.api.v1.model.blocks.BlockType
import org.apache.logging.log4j.kotlin.Logging

object NotionPageTreeCollector : Logging {

    private const val LEVEL_SEPARATOR = "--"
    private const val RETRIEVE_BLOCK_CHILDREN_PAGE_SIZE = 100

    fun collectPagesTree(client: NotionClient, tree: MutableList<String>, parent: NotionPageNode, blockId: String, currentLevel: Int) {
        val separator = LEVEL_SEPARATOR.repeat(currentLevel)
        val separatorNextLevel = LEVEL_SEPARATOR.repeat(currentLevel + 1)

        var nextCursor: String? = null
        var hasMore = true

        while (hasMore) {
            val children = try {
                client.retrieveBlockChildren(blockId, nextCursor, RETRIEVE_BLOCK_CHILDREN_PAGE_SIZE)
            }
            catch (e: Exception) {
                logger.error("Error when getting children for block $blockId", e)
                logger.error("$separator ERROR GETTING CHILDREN FOR BLOCK $blockId")
                return
            }

            children.results.forEach {
                var childNode: NotionPageNode? = null

                if (it.type == BlockType.ChildPage) { // only proceed if the child page is found
                    val childPage = it.asChildPage()

                    logger.info("$separatorNextLevel child page title: ${childPage.childPage.title}")

                    tree.add("$separatorNextLevel ${childPage.childPage.title}")

                    childNode = NotionPageNode(
                        parent.level + 1,
                        childPage.id!!,
                        parent.id,
                        childPage.childPage.title,
                        NotionPageUtils.parseNotionDateTimeString(childPage.lastEditedTime!!),
                        parent
                    )

                    parent.children.add(childNode)
                }

                if (
//                it.type != BlockType.ChildPage && // do NOT parse into the child pages
                    it.hasChildren == true
                ) { // block has child blocks -> handle them at the next level
                    val node = childNode ?: parent

                    collectPagesTree(client, tree, node, it.id!!, currentLevel + 1)
                }
            }

            // Update cursor and check if more results exist
            hasMore = children.hasMore
            nextCursor = children.nextCursor

            if (hasMore) {
                logger.info("Block $blockId has more child pages. Fetching next batch with cursor: \"$nextCursor\".")
            }
        }
    }
}
