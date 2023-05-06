package com.dv.telegram

import com.dv.telegram.tabs.TabType

enum class ResponseType {
    NOT_FOR_THE_BOT,
    ANSWER_NOT_FOUND, // TODO: what about "answer not found" + "No reply if answer not found"?
    SPECIAL_COMMAND,

    // the following values are the responses from the google sheet tabs
    COMMAND,
    WIKI_PAGE,
    YOUTUBE_VIDEO,
    CITY_CHAT,
    COUNTRY_CHAT;

    companion object {
        fun fromTabType(tabType: TabType): ResponseType {
            return when (tabType) {
                TabType.COMMANDS -> COMMAND
                TabType.WIKI_PAGES -> WIKI_PAGE
                TabType.YOUTUBE_VIDEOS -> YOUTUBE_VIDEO
                TabType.CITY_CHATS -> CITY_CHAT
                TabType.COUNTRY_CHATS -> COUNTRY_CHAT
            }
        }
    }
}
