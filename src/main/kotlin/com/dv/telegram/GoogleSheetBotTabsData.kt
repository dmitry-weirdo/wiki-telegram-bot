package com.dv.telegram

import com.dv.telegram.tabs.TabData
import com.dv.telegram.tabs.TabType

data class GoogleSheetBotTabsData(
    val commandTabs: List<TabData>,
    val dataTabs: List<TabData>
) {
    fun getCityChats(): TabData? = dataTabs
        .firstOrNull { it.tabConfig.tabType == TabType.CITY_CHATS }
}
