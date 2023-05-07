package com.dv.telegram.tabs

import com.dv.telegram.data.BotAnswerData
import com.dv.telegram.data.BotAnswerDataList
import com.dv.telegram.data.CityChatData
import com.dv.telegram.data.CityChatsDataList
import com.dv.telegram.data.WikiBotCommandData
import com.dv.telegram.data.WikiBotCommandsDataList
import com.dv.telegram.data.WikiPageData
import com.dv.telegram.data.WikiPagesDataList

/**
 * Conversion of [TabData]
 * to add the [BotAnswerDataList] functionality
 * instead of just [BotAnswerData].
 */
data class BotAnswerTabData<T : BotAnswerData> (
    val tabConfig: TabConfig,
    val tabAnswers: BotAnswerDataList<T>
) {
    companion object { // factory methods

        fun fromTabDataList(list: List<TabData>): List<BotAnswerTabData<BotAnswerData>> =
            list.map { fromTabData(it) }

        fun <T : BotAnswerData> fromTabData(tabData: TabData): BotAnswerTabData<T> {
            // todo: fix the ugly casting
            return when (tabData.tabConfig.tabFormat) {
                TabFormat.WIKI_PAGES -> {
                    val answers = tabData.answers as (List<WikiPageData>)
                    val tabAnswers = WikiPagesDataList(answers, tabData.responseType)

                    BotAnswerTabData(
                        tabData.tabConfig,
                        tabAnswers as BotAnswerDataList<WikiPageData>
                    ) as BotAnswerTabData<T>
                }

                TabFormat.CHATS -> {
                    val answers = tabData.answers as (List<CityChatData>) // todo: use common ChatData instead of CityChatData
                    val tabAnswers = CityChatsDataList(answers, tabData.responseType)

                    BotAnswerTabData(
                        tabData.tabConfig,
                        tabAnswers as BotAnswerDataList<CityChatData>
                    ) as BotAnswerTabData<T>
                }

                TabFormat.COMMANDS -> {
                    val answers = tabData.answers as (List<WikiBotCommandData>)
                    val tabAnswers = WikiBotCommandsDataList(answers, tabData.responseType)

                    BotAnswerTabData(
                        tabData.tabConfig,
                        tabAnswers as BotAnswerDataList<WikiBotCommandData>
                    ) as BotAnswerTabData<T>
                }
            }
        }
    }
}
