package com.dv.telegram

data class GoogleSheetBotData (
    val pages: List<WikiPageData>,
    val cityChats: List<CityChatData>,
    val countryChats: List<CountryChatData>,
    val commands: List<WikiBotCommandData>
)
