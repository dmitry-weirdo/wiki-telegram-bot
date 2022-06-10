package com.dv.telegram.command

import com.dv.telegram.WikiBot

class HelpSetting : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText <settingName>` — получить помощь по настройке с названием `<settingName>`."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/helpSetting"

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

        return "*${botSetting.name}*\n${botSetting.description}"
    }

    private fun unknownSettingResponse() = "Неизвестное имя настройки."
}
