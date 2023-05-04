package com.dv.telegram.command

import com.dv.telegram.WikiBot
import org.telegram.telegrambots.meta.api.objects.Update

class ListAdmins : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText` — вывести список администраторов бота, которые имеют право запускать специальные команды из списка в `${bot.botName} ${bot.specialCommands.listCommands.commandText}`.
        
        Команда `${bot.specialCommands.startCommand.commandText}` всегда доступна всем пользователям.
        """.trimIndent()

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/listAdmins"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val adminLines = mutableListOf<String>()

        for (botAdmin in bot.specialCommands.botAdmins) {
            adminLines.add(
                "— ${ getSettingValueForMarkdown(
                    BotCommandUtils.getClickableUserName(botAdmin)
                ) }" // escape _ in user names like "dmitry_weirdo"
            )
        }

        return adminLines.joinToString("\n")
    }
}
