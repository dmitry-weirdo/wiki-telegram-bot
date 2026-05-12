package com.dv.telegram.notion

data class NotionPageTreeCollectResult(
    val rootPageId: String,
    val rootPageTitle: String,
    val root: NotionPageNode,
    val startTime: Long, // todo: we may use ZDT etc if required
    val endTime: Long // todo: we may use ZDT etc if required
)