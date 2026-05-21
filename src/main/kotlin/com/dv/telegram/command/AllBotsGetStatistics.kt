package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.statistics.BotStatistics
import com.dv.telegram.util.DateUtils
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.InputStream

class AllBotsGetStatistics : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить статистику по работе всех ботов с момента текущего запуска окружения."

    override val defaultCommandName = "/allBotsGetStats"

    override fun returnFileInResponse() = true

    override fun getResponseFileName(context: BotContext): String {
        val currentTimeFormatted = DateUtils.getCurrentDateTimeInFileNameFormat()
        return "allBotsGetStats_$currentTimeFormatted.md"
    }

    override fun getResponseFileCaption(context: BotContext): String {
        return "Файл со статистикой по работе всех ботов."
    }

    override fun getResponse(text: String, bot: WikiBot, update: Update, context: BotContext): String {
        // todo: this is not necessary for commands that return files. We can return an empty string here.
        return buildFileBody(bot, update, context)
    }

    override fun getFileContent(text: String, bot: WikiBot, update: Update, context: BotContext): InputStream {
        val fileContentAsText = buildFileBody(bot, update, context)
        return getFileContent(fileContentAsText)
    }

    private fun buildFileBody(bot: WikiBot, update: Update, context: BotContext): String {
        val lines = mutableListOf<String>()

        val bots = bot.context.bots
            .sortedBy { it.botName }

        for (contextBot in bots) {
            val botTelegramName = contextBot.getTelegramUserName()
            val botTelegramNameForMarkdown = BotCommandUtils.getBotTelegramNameMarkdownLink(botTelegramName)

            val botGetStatisticsResponse = contextBot
                .specialCommands
                .getStatisticsCommand
                .getResponse("", contextBot, update, context)

            // no multi-line string because of indent problems when parameter string itself contains the line breaks
            lines.add("**${contextBot.botName}** ($botTelegramNameForMarkdown)\n$botGetStatisticsResponse")
        }

        // calculate aggregate statistics
        val statisticsFromAllBots = bots
            .map { it.statistics }

        val aggregateStatistics = BotStatistics.getAggregateStatistics(statisticsFromAllBots)

        lines.add("""
            **Суммарная статистика для ${bots.size} ботов:**
            ${GetStatistics.getStatisticsLine("Время старта ботов", aggregateStatistics.startTime)}
            ${GetStatistics.getStatisticsLine("Успешных запросов", aggregateStatistics.successfulRequestsCountWithPercentage)}
            ${GetStatistics.getStatisticsLine("Неуспешных запросов", aggregateStatistics.failedRequestsCountWithPercentage)}
            ${GetStatistics.getStatisticsLine("Всего запросов", aggregateStatistics.totalCallsWithPercentage)}
            ${GetStatistics.getStatisticsLine("Вызовов специальных команд", aggregateStatistics.specialCommandsCount)}
            ${GetStatistics.getStatisticsLine("Всего запросов (вместе со специальными командами)", aggregateStatistics.totalCallsWithSpecialCommands)}
        """.trimIndent())

        return lines.joinToString("  \n\n") { withMarkdownLineBreaks(it) }
    }

    private fun withMarkdownLineBreaks(text: String): String {
        // todo: compile pattern first?
        return text.replace(Regex("\r?\n"), "  \n")
    }
}
