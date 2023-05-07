package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class AllBotsList : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить список всех ботов, запущенных в текущем окружении."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/allBotsList"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val lines = mutableListOf<String>()

        val bots = bot.context.bots
            .sortedBy { it.botName }

        lines.add("*Всего ботов: ${bots.size}*")

        for (contextBot in bots) {
            val botTelegramName = contextBot.getTelegramUserName()
            val clickableBotTelegramName = BotCommandUtils.getClickableUserName(botTelegramName)
            val botTelegramNameForMarkdown = getSettingValueForMarkdown(clickableBotTelegramName)

            val botCommandTabNames = contextBot.getCommandTabNames()

            lines.add(
                """
                *${contextBot.botName}* ($botTelegramNameForMarkdown)
                Вкладки с командами: `$botCommandTabNames`
                Окружение: ${contextBot.environmentName}
                """.trimIndent()
            )
        }

        return lines.joinToString("\n\n")
    }
}
