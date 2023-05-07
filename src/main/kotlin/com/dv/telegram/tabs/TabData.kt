package com.dv.telegram.tabs

import com.dv.telegram.ResponseType
import com.dv.telegram.data.BotAnswerData

data class TabData( // todo: think about naming
    val tabConfig: TabConfig,
    val answers: List<BotAnswerData>
) {
    val responseType: ResponseType = ResponseType.fromTabConfig(tabConfig)
}
