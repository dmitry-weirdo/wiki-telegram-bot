package com.dv.telegram

import com.dv.telegram.tabs.TabData

data class GoogleSheetBotTabsData(
    val commandTabs: List<TabData>,
    val dataTabs: List<TabData>
)
