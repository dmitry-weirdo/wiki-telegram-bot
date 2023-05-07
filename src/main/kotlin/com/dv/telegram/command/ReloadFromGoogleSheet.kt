package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.JacksonUtils
import org.telegram.telegrambots.meta.api.objects.Update

class ReloadFromGoogleSheet : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — перезагрузить конфигурацию бота из Google Sheet."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/gs-reload-5150" // "secret" name to not be guessed by the user

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val reloadedSuccessful = bot.reloadBotDataFromGoogleSheet()

        val tabConfigsString = JacksonUtils.serializeToString(bot.tabConfigs, true)

        return if (reloadedSuccessful) {
            "Данные бота успешно загружены из Google Sheet.\n\nКонфигурация вкладок бота:\n```$tabConfigsString```"
        }
        else {
            "При загрузке данных из Google Sheet произошла ошибка.\n\nКонфигурация вкладок бота:\n```$tabConfigsString```"
        }
    }
}
