package com.dv.telegram.data

import com.dv.telegram.GoogleSheetBotData

class WikiBotCommandsDataList(answers: List<WikiBotCommandData>) : BotAnswerDataList<WikiBotCommandData>(answers) {

    constructor(botData: GoogleSheetBotData) : this(botData.commands)

    override fun getResponseText(matches: List<WikiBotCommandData>): String {
        if (matches.size == 1) {
            return matches[0].getOneLineAnswer()
        }

        return matches
            .joinToString("\n") { it.getMultiLineAnswer() } // passing transform parameter to joinToString
    }
}
