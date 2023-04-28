package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.JacksonUtils
import org.telegram.telegrambots.meta.api.objects.Update

class GetChatInfo : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText` — выдать информацию о текущем чате, доступную из сообщения пользователя.

        См. https://core.telegram.org/bots/api#chat
        """.trimIndent()

    override val defaultCommandName = "/getChatInfo"

    override fun getResponse(text: String, bot: WikiBot, update: Update) =
        JacksonUtils.serializeToString(update.message.chat, true)
}
