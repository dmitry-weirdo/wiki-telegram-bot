package com.dv.telegram.command

import com.dv.telegram.WikiBot

class GetFailedRequests : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить список разных неуспешных вызовов бота с момента текущего запуска инстанса или с момента очистки этого списка."

    override val defaultCommandName = "/getFailedRequests"

    override fun getResponse(text: String, bot: WikiBot): String {
        val failedRequestsLines = bot
            .statistics
            .failedRequests
            .map { "— $it" }

        val totalLine = "Разных неудачных запросов: ${failedRequestsLines.size}"

        val responseLines = mutableListOf<String>()
        responseLines.add(totalLine)
        responseLines.addAll(failedRequestsLines)

        return responseLines.joinToString("\n")
    }
}
