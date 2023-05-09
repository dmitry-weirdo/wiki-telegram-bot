package com.dv.telegram.tabs

data class TabConfig(
    val tabName: String,
    val tabFormat: TabFormat,
    val tabType: TabType,

    val showHeader: Boolean = false,
    val header: String? = null,
    val bullet: String = "▫" // without space
)
