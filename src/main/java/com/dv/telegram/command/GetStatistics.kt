package com.dv.telegram.command

import com.dv.telegram.WikiBot
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GetStatistics : BasicBotCommand() {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss") // todo: probably move to companion object

    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить статистику по работе бота с момента текущего запуска инстанса."

    override val defaultCommandName = "/getStats"

    override fun getResponse(text: String, bot: WikiBot): String {
        val statistics = bot.statistics

        val statisticsLines = listOf(
            getStatisticsLine("Время старта бота", statistics.startTime),
            getStatisticsLine("Успешных запросов", statistics.successfulRequestsCountWithPercentage),
            getStatisticsLine("Неуспешных запросов", statistics.failedRequestsCountWithPercentage),
            getStatisticsLine("Всего запросов", statistics.totalCallsWithPercentage),
            getStatisticsLine("Вызовов специальных команд", statistics.specialCommandsCount),
            getStatisticsLine("Всего запросов (вместе со специальными командами)", statistics.totalCallsWithSpecialCommands)
        )

        return statisticsLines.joinToString("\n")
    }

    private fun getStatisticsLine(name: String, count: Long) =
        "— $name: $count"

    private fun getStatisticsLine(name: String, value: String) =
        "— $name: $value"

    private fun getStatisticsLine(name: String, time: ZonedDateTime) =
        "— $name: ${dateTimeFormatter.format(time)}"
}
