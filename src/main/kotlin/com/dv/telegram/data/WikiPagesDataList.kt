package com.dv.telegram.data

import com.dv.telegram.ResponseType

class WikiPagesDataList(answers: List<WikiPageData>, responseType: ResponseType) : BotAnswerDataList<WikiPageData>(answers, responseType) {

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
