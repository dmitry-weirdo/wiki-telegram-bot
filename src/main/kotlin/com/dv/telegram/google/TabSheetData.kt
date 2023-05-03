package com.dv.telegram.google

import com.dv.telegram.tabs.TabConfig

/**
 * [SheetData] with additional metadata describing which type of tab it is.
 */
data class TabSheetData(
    val config: TabConfig,
    val sheet: SheetData
)
