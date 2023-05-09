package com.dv.telegram.data

import com.dv.telegram.tabs.TabData

class ChatsDataList(answers: List<ChatData>, tabData: TabData) : BotAnswerDataList<ChatData>(answers, tabData) {

    override fun getResponseText(matches: List<ChatData>): String {
        return matches
            .joinToString("\n\n") { it.chatsAnswer } // passing transform parameter to joinToString
    }
}
