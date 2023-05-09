package com.dv.telegram.data

import com.dv.telegram.tabs.TabData

class WikiPagesDataList(answers: List<WikiPageData>, tabData: TabData) : BotAnswerDataList<WikiPageData>(answers, tabData) {

    override fun getResponseText(matches: List<WikiPageData>, forceBullet: Boolean): String {
        if (matches.size == 1) {
            return if (forceBullet) { // single answer has bullet only when forced
                matches[0].getAnswerWithBullet(bullet)
            }
            else {
                matches[0].getAnswerWithoutBullet()
            }
        }

        // multiline answers have bullets in any case
        return matches
            .joinToString("\n") { it.getAnswerWithBullet(bullet) } // passing transform parameter to joinToString
    }
}
