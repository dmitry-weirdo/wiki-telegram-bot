package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class ClearSuccessfulRequests : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — очистить список разных успешных вызовов бота с момента текущего запуска инстанса или с момента очистки этого списка."

    override val defaultCommandName = "/clearSuccessfulRequests"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val clearedSuccessfulRequestsCount = bot.statistics.successfulRequests.size

        bot.statistics.clearSuccessfulRequests()

        return "Список из $clearedSuccessfulRequestsCount удачных запросов к боту очищен."
    }
}
