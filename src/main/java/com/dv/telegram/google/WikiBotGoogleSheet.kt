package com.dv.telegram.google

data class WikiBotGoogleSheet( // only contains strings from the Google Sheet, not semantically parsed
    val wikiPagesSheet: SheetData,
    val cityChatsSheet: SheetData,
    val countryChatsSheet: SheetData,
    val commandsSheet: SheetData
)
