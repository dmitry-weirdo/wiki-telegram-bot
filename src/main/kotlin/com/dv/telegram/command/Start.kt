package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.config.StartMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.text.MessageFormat

class Start : BasicBotCommand() {
    override val name: String = javaClass.simpleName // class defined explicitly because of Java -> Kotlin class conversion

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText` — выдать приветственное сообщение бота. Сообщение определяется настройкой *${StartMessage.NAME}*.

        Команда также работает без имени бота: `$commandText`.
        """.trimIndent()

    override val defaultCommandName = "/start"

    // /start is a standard command, cannot be overridden
    override var commandName
        get() = super.commandName
        set(commandName) = // /start is a standard command, cannot be overridden
            throw UnsupportedOperationException("Cannot override $defaultCommandName command name.")

    override fun requiresBotAdminRights() = false // /start command should be available for non-admin users too

    override fun getResponse(text: String, bot: WikiBot, update: Update): String = // class defined explicitly because of Java -> Kotlin class conversion
        // todo: use some Kotlin formatter instead of Java if exists and possible
        MessageFormat.format(
            bot.settings.startMessage,
            bot.botName
        )
}
