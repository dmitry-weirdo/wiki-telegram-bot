package com.dv.telegram.command

import com.dv.telegram.WikiBot

class GetSetting : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText <settingName>` — получить значение настройки с названием `<settingName>`."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/getSetting"

    override fun getResponse(text: String, bot: WikiBot): String {
        val commandStartIndex = text.indexOf(commandText)
        if (commandStartIndex < 0) {
            return unknownSettingResponse()
        }

        val commandEndIndex = commandStartIndex + commandText.length
        if (commandEndIndex >= text.length) {
            return unknownSettingResponse()
        }

        val settingName = text.substring(commandEndIndex).trim()

        val botSetting = bot.settings.getBotSetting(settingName)
            ?: return unknownSettingResponse()

        val value = getSettingValueForMarkdown(botSetting)
        return "*${botSetting.name}*\n$value"
    }

    private fun unknownSettingResponse() = "Неизвестное имя настройки."
}
