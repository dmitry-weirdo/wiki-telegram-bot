package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.DateUtils
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

class AllBotsGetFailedRequests : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить список разных неуспешных вызовов всех ботов с момента текущего запуска окружения или с момента очистки этих списков в том или ином боте."

    override val defaultCommandName = "/allBotsGetFailedRequests"

    override fun returnFileInResponse() = true

    override fun getResponseFileName(context: BotContext): String {
        val currentTimeFormatted = DateUtils.getCurrentDateTimeInFileNameFormat()
        return "allBotsGetFailedRequests_$currentTimeFormatted.txt"
    }

    override fun getResponseFileCaption(context: BotContext): String {
        return "Файл с неуспешными запросами для всех ботов."
    }

    override fun getResponse(text: String, bot: WikiBot, update: Update, context: BotContext): String {
        // todo: this is not necessary for commands that return files. We can return an empty string here.
        return buildFileBody(text, bot, update, context)
    }

    override fun getFileContent(text: String, bot: WikiBot, update: Update, context: BotContext): InputStream {
        val fileContentAsText = buildFileBody(text, bot, update, context)
        return getFileContent(fileContentAsText)
    }

    private fun buildFileBody(text: String, bot: WikiBot, update: Update, context: BotContext): String {
        val lines = mutableListOf<String>()

        val bots = bot.context.bots
            .sortedBy { it.botName }

        val failedRequests = mutableSetOf<String>() // won't be repeats since all bots have different names. But probably for the modes triggering without bot name there may be duplicates

        // no Markdown because of user input in the failedRequests
        for (contextBot in bots) {
            failedRequests.addAll(contextBot.statistics.failedRequests)

            val botTelegramName = contextBot.getTelegramUserName()
            val botTelegramNameForResponse = BotCommandUtils.getClickableUserName(botTelegramName)

            val botGetFailedRequestsResponse = contextBot
                .specialCommands
                .getFailedRequestsCommand
                .getResponse("", contextBot, update, context)

            // no multi-line string because of indent problems when parameter string itself contains the line breaks
            lines.add("${contextBot.botName} ($botTelegramNameForResponse)\n$botGetFailedRequestsResponse")
        }

        lines.add("Всего ботов: ${bots.size}\nВсего разных неудачных запросов: ${failedRequests.size}")

        return lines.joinToString("\n\n")
    }
}
