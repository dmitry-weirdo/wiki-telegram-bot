package com.dv.telegram.google

data class WikiBotGoogleSheetTabsData( // only contains strings from the Google Sheet, not semantically parsed
    val commandSheets: List<TabSheetData>,
    val dataSheets: List<TabSheetData>
)
