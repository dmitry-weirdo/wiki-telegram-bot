package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.DateUtils
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

class AllBotsGetSuccessfulRequests : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить список разных успешных вызовов всех ботов с момента текущего запуска окружения или с момента очистки этих списков в том или ином боте."

    override val defaultCommandName = "/allBotsGetSuccessfulRequests"

    override fun returnFileInResponse() = true

    override fun getResponseFileName(context: BotContext): String {
        val currentTimeFormatted = DateUtils.getCurrentDateTimeInFileNameFormat()
        return "allBotsGetSuccessfulRequests_$currentTimeFormatted.txt"
    }

    override fun getResponseFileCaption(context: BotContext): String {
        return "Файл с успешными запросами для всех ботов."
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

        val successfulRequests = mutableSetOf<String>() // won't be repeats since all bots have different names. But probably for the modes triggering without bot name there may be duplicates

        // no Markdown because of user input in the successfulRequests
        for (contextBot in bots) {
            successfulRequests.addAll(contextBot.statistics.successfulRequests)

            val botTelegramName = contextBot.getTelegramUserName()
            val botTelegramNameForResponse = BotCommandUtils.getClickableUserName(botTelegramName)

            val botGetSuccessfulRequestsResponse = contextBot
                .specialCommands
                .getSuccessfulRequestsCommand
                .getResponse("", contextBot, update, context)

            // no multi-line string because of indent problems when parameter string itself contains the line breaks
            lines.add("${contextBot.botName} ($botTelegramNameForResponse)\n$botGetSuccessfulRequestsResponse")
        }

        lines.add("Всего ботов: ${bots.size}\nВсего разных удачных запросов: ${successfulRequests.size}")

        return lines.joinToString("\n\n")
    }
}
