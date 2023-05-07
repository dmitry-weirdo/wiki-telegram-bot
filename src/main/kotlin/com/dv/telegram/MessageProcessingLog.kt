package com.dv.telegram

import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User

data class MessageProcessingLog( // to be logged in a JSON
    val botName: String, // multiple bots can be run on the same instance
    val botTelegramName: String, // multiple bots can be run on the same instance

    // telegram API metadata
    val date: Int, // unix time
    val chat: Chat,
    val user: User?, // who called the bot, is nullable in the API
    // todo: think whether we need messages not for the bot

    val request: String, // we don't handle the empty/bland message
    val response: String?, // can be pretty big, but who cares
    var responseTypes: List<ResponseType>,
    var matchedKeywords: List<String>,
    // todo: do we need separate "tab names" or "responseTypes" is enough?
    val answerIsFound: Boolean
) {
    companion object { // factory methods

        @JvmStatic
        fun create(
            bot: WikiBot,
            update: Update,
            result: MessageProcessingResult
        ): MessageProcessingLog {
            return MessageProcessingLog(
                botName = bot.botName,
                botTelegramName = bot.getTelegramUserName(),

                date = update.message.date,
                chat = update.message.chat,
                user = update.message.from,

                request = update.message.text!!,

                response = result.response,
                responseTypes = result.responseTypes,
                matchedKeywords = result.matchedKeywords,
                answerIsFound = result.answerIsFound
            )
        }
    }
}
