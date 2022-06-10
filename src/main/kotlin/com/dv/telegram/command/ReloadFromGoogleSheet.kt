package com.dv.telegram.command

import com.dv.telegram.WikiBot

class ReloadFromGoogleSheet : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — перезагрузить конфигурацию бота из Google Sheet."

    override val defaultCommandName = "/gs-reload-5150" // "secret" name to not be guessed by the user

    override fun getResponse(text: String, bot: WikiBot): String {
        val reloadedSuccessful = bot.reloadBotDataFromGoogleSheet()

        return if (reloadedSuccessful)
            "Данные бота успешно загружены из Google Sheet."
        else
            "При загрузке данных из Google Sheet произошла ошибка."
    }
}
