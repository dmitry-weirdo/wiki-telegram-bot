package com.dv.telegram.data

import com.dv.telegram.GoogleSheetBotData

class WikiPagesDataList(answers: List<WikiPageData>) : BotAnswerDataList<WikiPageData>(answers) {

    constructor(botData: GoogleSheetBotData) : this(botData.pages)

    override fun getResponseText(matches: List<WikiPageData>): String? {
        if (matches.isEmpty()) {
            return null
        }

        if (matches.size == 1) {
            return matches[0].getOneLineAnswer()
        }

        return matches
            .joinToString("\n") { it.getMultiLineAnswer() } // passing transform parameter to joinToString
    }
}
