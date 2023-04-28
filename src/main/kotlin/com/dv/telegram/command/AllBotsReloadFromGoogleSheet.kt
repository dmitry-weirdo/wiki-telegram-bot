package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class AllBotsReloadFromGoogleSheet : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — перезагрузить конфигурацию всех ботов окружения из Google Sheet."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/allBotsReloadConfigs"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val lines = mutableListOf<String>()

        val bots = bot.context.bots
            .sortedBy { it.botName }

        for (contextBot in bots) {
            val botTelegramName = contextBot.getTelegramUserName()
            val botTelegramNameForMarkdown = getSettingValueForMarkdown(BotCommandUtils.getClickableUserName(botTelegramName))

            val botReloadConfigResponse = contextBot
                .specialCommands
                .reloadFromGoogleSheetCommand
                .getResponse("", contextBot, update)

            // no multi-line string because of indent problems when parameter string itself contains the line breaks
            lines.add("*${contextBot.botName}* ($botTelegramNameForMarkdown)\n$botReloadConfigResponse")
        }

        lines.add("*Данные ${bots.size} ботов загружены из Google Sheet.*")

        return lines.joinToString("\n\n")
    }
}
