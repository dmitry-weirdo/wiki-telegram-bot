package com.dv.telegram.notion

import java.time.ZonedDateTime

data class NotionPageTreeCollectResult(
    val rootPageId: String,
    val rootPageTitle: String,
    val root: NotionPageNode,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime
)
