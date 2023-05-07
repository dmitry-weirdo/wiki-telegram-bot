package com.dv.telegram

import com.dv.telegram.tabs.TabConfigs

@Suppress("LongParameterList")
class WikiBotConfig(
    val botName: String = "",
    val botToken: String = "",
    val environmentName: String = "",

    // Google Sheets
    val googleSheetsApiKey: String = "",
    val googleSpreadsheetId: String = "",

    // see https://developers.google.com/sheets/api/guides/concepts
    // todo: use sheets by number instead if it is possible with A1 or R1C1 notation
    val sheets: TabConfigs = TabConfigs(),

    // Notion
    val notionToken: String = "",
    val cityChatsPageId: String = "9a0effe48cf34cd49c849a9e05c61fb9", // список чатов по городам (https://uahelp.wiki/german-city-chats)
    val cityChatsToggleHeading1Text: String = "Чаты по землям и городам Германии (Telegram, WhatsApp)",

    // special commands
    val botAdmins: List<String> = listOf(), // Telegram users that are allowed to execute the special commands

    val commands: Map<String, String> = mapOf(), // special commands

    val settings: MutableMap<String?, String?> = mutableMapOf()
)
