package com.dv.telegram.command

import com.dv.telegram.config.BotSetting

abstract class BasicBotCommand : BotCommand {
    override var commandName: String? = null // interface only defines abstract getter and setter, not the field itself

    override fun useMarkdownInResponse() = false

    override fun returnFileInResponse() = false

    override fun getResponseFileName(): String {
        throw UnsupportedOperationException("Cannot get response file name for bot commands that do not return the file.")
    }

    override fun getResponseFileCaption(): String {
        throw UnsupportedOperationException("Cannot get response file caption for bot commands that do not return the file.")
    }

    companion object {
        @JvmStatic
        fun getSettingValueForMarkdown(botSetting: BotSetting<*>): String {
            return getSettingValueForMarkdown(
                botSetting.getValue().toString()
            )
        }

        @JvmStatic
        fun getSettingValueForMarkdown(settingValue: String): String {
            return settingValue.replace("\\_".toRegex(), "\\\\_") // for Markdown, escape "_" as "\_" to not fail sending the Telegram message
        }
    }
}
