package com.dv.telegram.notion

data class NotionCityChatsImportResult(
    var pageId: String,
    var pageTitle: String,
    var toggleHeading1Text: String,
    var totalCities: Int,
    var totalChats: Int
)
