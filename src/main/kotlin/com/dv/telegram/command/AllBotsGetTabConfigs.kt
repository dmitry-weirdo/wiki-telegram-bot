package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.JacksonUtils
import org.telegram.telegrambots.meta.api.objects.Update

class AllBotsGetTabConfigs : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — вывести конфигурации вкладок Google Sheet для всех ботов."

    override val defaultCommandName = "/allBotsGetTabConfigs"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val lines = mutableListOf<String>()

        val bots = bot.context.bots
            .sortedBy { it.botName }

        // no markdown because of user input in the successfulRequests
        for (contextBot in bots) {
            val botTelegramName = contextBot.getTelegramUserName()
            val botTelegramNameForResponse = BotCommandUtils.getClickableUserName(botTelegramName)

            val tabConfigsString = JacksonUtils.serializeToString(bot.tabConfigs, true)

            // no multi-line string because of indent problems when parameter string itself contains the line breaks
            lines.add("${contextBot.botName} ($botTelegramNameForResponse)\n$tabConfigsString")
        }

        lines.add("Всего ботов: ${bots.size}")

        return lines.joinToString("\n\n")
    }
}
