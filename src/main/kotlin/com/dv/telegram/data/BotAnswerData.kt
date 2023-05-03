package com.dv.telegram.data

/**
 * A data parsed from a single row in a Google Sheet bot config.
 * Multiple rows of a single tab are grouped into [BotAnswerDataList].
 */
interface BotAnswerData {
    fun isPresentIn(text: String): Boolean
}
