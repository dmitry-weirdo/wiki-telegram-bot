package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.JacksonUtils
import org.telegram.telegrambots.meta.api.objects.Update

class GetLastMessageLog : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText` — выдать лог последнего сообщения к боту (из всех чатов)
        """.trimIndent()

    override val defaultCommandName = "/getLastMessage"

    // todo: we can add the option to pass the id of the user (id can be gotten from Rose)
    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        return if (bot.lastMessageLog != null) {
            JacksonUtils.serializeToString(bot.lastMessageLog!!, true)
        }
        else {
            "${bot.botName} ещё не получил ни одного сообщения."
        }
    }
}
