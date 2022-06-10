package com.dv.telegram.command

import com.dv.telegram.WikiBot

class ListSettings : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — вывести список изменяемых в рантайме настроек бота."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/listSettings"

    override fun getResponse(text: String, bot: WikiBot): String {
        val settingsLines = mutableListOf<String>()

        for (setting in bot.settings.settings.values) {
            settingsLines.add(
                "— *${setting.name}*:\n${getSettingValueForMarkdown(setting)}",
            )
        }

        return settingsLines.joinToString("\n\n")
    }
}
