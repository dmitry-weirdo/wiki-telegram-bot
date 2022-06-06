package com.dv.telegram.command

import com.dv.telegram.WikiBot

class GetEnvironment : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — выдать окружение, в котором запущен инстанс бота."

    override val defaultCommandName = "/getEnvironment"

    override fun getResponse(text: String, bot: WikiBot) =
        "${bot.botName} живёт здесь: ${bot.environmentName}."
}
