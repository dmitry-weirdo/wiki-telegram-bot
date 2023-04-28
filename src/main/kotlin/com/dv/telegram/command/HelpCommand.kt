package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class HelpCommand : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText <commandName>` — получить помощь по команде с названием `<commandName>`."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName: String
        get() = "/helpCommand"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val commandStartIndex = text.indexOf(commandText)
        if (commandStartIndex < 0) {
            return unknownCommandResponse()
        }

        val commandEndIndex = commandStartIndex + commandText.length
        if (commandEndIndex >= text.length) {
            return unknownCommandResponse()
        }

        val commandText = text.substring(commandEndIndex).trim()

        val botCommand = bot
            .specialCommands
            .getCommand(commandText)
            ?: return unknownCommandResponse()

        return "*${botCommand.commandText}*\n${botCommand.getDescription(bot)}"
    }

    private fun unknownCommandResponse() = "Неизвестное имя команды."
}
