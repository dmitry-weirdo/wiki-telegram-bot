package com.dv.telegram.notion

data class NotionPageNode(
    val id: String,
    val parentId: String,
    val title: String,

    val children: MutableList<NotionPageNode> = mutableListOf()
)
