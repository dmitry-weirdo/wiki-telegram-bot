package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class GetSuccessfulRequests : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить список разных успешных вызовов бота с момента текущего запуска инстанса или с момента очистки этого списка."

    override val defaultCommandName = "/getSuccessfulRequests"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val successfulRequestsLines = bot
            .statistics
            .successfulRequests
            .map { "— $it" }

        val totalLine = "Разных удачных запросов: ${successfulRequestsLines.size}"

        val responseLines = mutableListOf<String>()
        responseLines.add(totalLine)
        responseLines.addAll(successfulRequestsLines)

        return responseLines.joinToString("\n")
    }
}
