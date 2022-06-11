package com.dv.telegram.command

import com.dv.telegram.WikiBot

class AllBotsList: BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить список всех ботов, запущенных в текущем окружении."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/allBotsList"

    override fun getResponse(text: String, bot: WikiBot): String {
        val lines = mutableListOf<String>()

        val bots = bot.context.bots
            .sortedBy { it.botName }

        lines.add("*Всего ботов: ${bots.size}*")

        for (contextBot in bots) {
            // todo: bot telegram name (clickable form, as in ListAdmins)
            lines.add("""
                *${contextBot.botName}*
                Лист со списком команд: ${contextBot.commandSheetName}
                Окружение: ${contextBot.environmentName}
            """.trimIndent())
        }

        return lines.joinToString("\n\n")
    }
}
