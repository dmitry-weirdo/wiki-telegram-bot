package com.dv.telegram.tabs

import com.dv.telegram.ResponseType

/**
 * Affect the value set for the logs as a [ResponseType].
 */
enum class TabType {
    COMMANDS, // todo: think whether we should split "common commands" and "bot private commands"
    WIKI_PAGES,
    YOUTUBE_VIDEOS,
    CITY_CHATS,
    COUNTRY_CHATS
}
