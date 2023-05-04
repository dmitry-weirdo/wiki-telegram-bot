package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class ListCommands : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — получить список команд бота."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/listCommands"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val lines = mutableListOf<String>()

        lines.add("Список команд бота:")

        for (command in bot.specialCommands.commands) {
            lines.add(
                "— `${bot.botName} ${command.commandText}`"
            )
        }

        lines.add(
            "Для получения справки по команде используйте команду\n`${bot.botName} ${bot.specialCommands.helpCommand.commandText} <commandName>`"
        )

        return lines.joinToString("\n\n")
    }
}
