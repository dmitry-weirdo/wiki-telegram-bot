package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.statistics.BotStatistics

class AllBotsGetStatistics : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить статистику по работе всех ботов с момента текущего запуска окружения."

    override val defaultCommandName = "/allBotsGetStats"

    override fun useMarkdownInResponse() = true

    override fun getResponse(text: String, bot: WikiBot): String {
        val lines = mutableListOf<String>()

        val bots = bot.context.bots
            .sortedBy { it.botName }

        for (contextBot in bots) {
            val botTelegramName = contextBot.getTelegramUserName()
            val botTelegramNameForMarkdown = getSettingValueForMarkdown(BotCommandUtils.getClickableUserName(botTelegramName))

            val botGetStatisticsResponse = contextBot
                .specialCommands
                .getStatisticsCommand
                .getResponse("", contextBot)

            // no multi-line string because of indent problems when parameter string itself contains the line breaks
            lines.add("*${contextBot.botName}* ($botTelegramNameForMarkdown)\n$botGetStatisticsResponse")
        }

        // calculate aggregate statistics
        val statisticsFromAllBots = bots
            .map{ it.statistics }

        val aggregateStatistics = BotStatistics.getAggregateStatistics(statisticsFromAllBots)

        lines.add("""
            *Суммарная статистика для ${bots.size} ботов:*
            ${GetStatistics.getStatisticsLine("Время старта ботов", aggregateStatistics.startTime)}
            ${GetStatistics.getStatisticsLine("Успешных запросов", aggregateStatistics.successfulRequestsCountWithPercentage)}
            ${GetStatistics.getStatisticsLine("Неуспешных запросов", aggregateStatistics.failedRequestsCountWithPercentage)}
            ${GetStatistics.getStatisticsLine("Всего запросов", aggregateStatistics.totalCallsWithPercentage)}
            ${GetStatistics.getStatisticsLine("Вызовов специальных команд", aggregateStatistics.specialCommandsCount)}
            ${GetStatistics.getStatisticsLine("Всего запросов (вместе со специальными командами)", aggregateStatistics.totalCallsWithSpecialCommands)}
        """.trimIndent())

        return lines.joinToString("\n\n")
    }
}
