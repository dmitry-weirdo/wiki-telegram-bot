package com.dv.telegram.tabs

data class TabConfig(
    val tabName: String,
    val tabFormat: TabFormat,
    val tabType: TabType,

    val showHeader: Boolean = true, // todo: think whether we need this
    val header: String? = null // todo: think whether we need this
)
