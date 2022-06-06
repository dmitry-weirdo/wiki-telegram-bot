package com.dv.telegram.command

import com.dv.telegram.WikiBot

class ClearFailedRequests : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — очистить список разных неуспешных вызовов бота с момента текущего запуска инстанса или с момента очистки этого списка."

    override val defaultCommandName = "/clearFailedRequests"

    override fun getResponse(text: String, bot: WikiBot): String {
        val clearedFailedRequestsCount = bot.statistics.failedRequests.size

        bot.statistics.clearFailedRequests()

        return "Список из $clearedFailedRequestsCount неудачных запросов к боту очищен."
    }
}
