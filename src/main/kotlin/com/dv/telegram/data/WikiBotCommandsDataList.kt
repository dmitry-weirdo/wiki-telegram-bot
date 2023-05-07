package com.dv.telegram.data

import com.dv.telegram.tabs.TabData

class WikiBotCommandsDataList(answers: List<WikiBotCommandData>, tabData: TabData) : BotAnswerDataList<WikiBotCommandData>(answers, tabData) {

    override fun getResponseText(matches: List<WikiBotCommandData>): String {
        if (matches.size == 1) {
            return matches[0].getOneLineAnswer()
        }

        return matches
            .joinToString("\n") { it.getMultiLineAnswer() } // passing transform parameter to joinToString
    }
}
