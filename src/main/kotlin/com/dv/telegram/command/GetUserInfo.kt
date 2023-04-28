package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.JacksonUtils
import org.telegram.telegrambots.meta.api.objects.Update

class GetUserInfo : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText` — выдать информацию о текущем пользователе, который вызвал команду.

        См. https://core.telegram.org/bots/api#user
        """.trimIndent()

    override val defaultCommandName = "/getUserInfo"

    // todo: we can add the option to pass the id of the user (id can be gotten from Rose)
    override fun getResponse(text: String, bot: WikiBot, update: Update) =
        JacksonUtils.serializeToString(update.message.from, true)
}
