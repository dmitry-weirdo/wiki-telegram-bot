package com.dv.telegram.notion

data class NotionPageNode(
    var level: Int,
    val id: String,
    val parentId: String,
    val title: String,

    val parent: NotionPageNode?,
    val children: MutableList<NotionPageNode> = mutableListOf()
) {
    fun getLink(): String {
        return NotionPageUtils.getPageNotionLink(id)!!
    }

    fun getParentLink(): String { // will return just https://www.notion.so/ in case of empty parent
        return NotionPageUtils.getPageNotionLink(parentId)!!
    }

    fun getSeparator(separator: String = DEFAULT_SEPARATOR): String {
        return separator.repeat(level - ROOT_LEVEL)
    }

    companion object {
        const val ROOT_LEVEL = 0
        const val DEFAULT_SEPARATOR = "        "
    }
}
