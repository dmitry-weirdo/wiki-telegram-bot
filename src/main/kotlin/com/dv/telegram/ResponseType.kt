package com.dv.telegram

enum class ResponseType {
    NOT_FOR_THE_BOT,
    ANSWER_NOT_FOUND, // TODO: what about "answer not found" + "No reply if answer not found"?
    SPECIAL_COMMAND,
    COMMAND,
    WIKI_PAGE,
    CITY_CHAT,
    COUNTRY_CHAT
}
