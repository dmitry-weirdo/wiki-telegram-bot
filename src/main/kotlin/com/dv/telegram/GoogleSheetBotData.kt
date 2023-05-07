package com.dv.telegram

import com.dv.telegram.data.CityChatData
import com.dv.telegram.data.CountryChatData
import com.dv.telegram.data.WikiBotCommandData
import com.dv.telegram.data.WikiPageData

@Deprecated("Use [GoogleSheetBotTabsData] instead.")
data class GoogleSheetBotData(
    val pages: List<WikiPageData>,
    val cityChats: List<CityChatData>,
    val countryChats: List<CountryChatData>,
    val commands: List<WikiBotCommandData>
)
