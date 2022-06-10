package com.dv.telegram.command

import com.dv.telegram.WikiBot

class SetSetting : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText <settingName> <settingValue>` — установить значение настройки с названием `<settingName>` в значение `<settingValue>`."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/setSetting"

    override fun getResponse(text: String, bot: WikiBot): String {
        val commandStartIndex = text.indexOf(commandText)
        if (commandStartIndex < 0) {
            return unknownSettingResponse()
        }

        val commandEndIndex = commandStartIndex + commandText.length
        if (commandEndIndex >= text.length) {
            return unknownSettingResponse()
        }

        val settingNameAndValue = text.substring(commandEndIndex).trim()

        val nameValueSeparator = " "
        val separatorIndex = settingNameAndValue.indexOf(nameValueSeparator)

        if (separatorIndex < 0 || separatorIndex >= settingNameAndValue.length) {
            return unknownSettingResponse() // todo: probably another more concrete response
        }

        val settingName = settingNameAndValue.substring(0, separatorIndex).trim()
        val settingValue = settingNameAndValue.substring(separatorIndex).trim()

        val botSetting = bot.settings.getBotSetting(settingName)
            ?: return unknownSettingResponse()

        return try {
            botSetting.setValue(settingValue)
            bot.settings.fillSettingCacheFields()

            "*${botSetting.name}* установлена в значение\n${getSettingValueForMarkdown(settingValue)}"
        }
        catch (e: Exception) {
            "Ошибка при установке настройки *${botSetting.name}* в значение\n${getSettingValueForMarkdown(settingValue)}"
        }
    }

    private fun unknownSettingResponse(): String = "Неизвестное имя настройки."
}
