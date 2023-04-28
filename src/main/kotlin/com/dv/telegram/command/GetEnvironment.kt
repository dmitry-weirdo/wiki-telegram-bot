package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class GetEnvironment : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — выдать окружение, в котором запущен инстанс бота."

    override val defaultCommandName = "/getEnvironment"

    override fun getResponse(text: String, bot: WikiBot, update: Update) =
        "${bot.botName} живёт здесь: ${bot.environmentName}."
}
