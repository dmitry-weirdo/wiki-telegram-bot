package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.config.BotSetting
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

abstract class BasicBotCommand : BotCommand {
    override var commandName: String? = null // interface only defines abstract getter and setter, not the field itself

    override fun useMarkdownInResponse() = false

    override fun returnFileInResponse() = false

    override fun getResponseFileName(context: BotContext): String {
        throw UnsupportedOperationException("Cannot get response file name for bot commands that do not return the file.")
    }

    override fun getResponseFileCaption(context: BotContext): String {
        throw UnsupportedOperationException("Cannot get response file caption for bot commands that do not return the file.")
    }

    override fun getFileContent(text: String, bot: WikiBot, update: Update, context: BotContext): InputStream? = null

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

        @JvmStatic
        fun getFileContent(fileContentAsText: String): InputStream {
            return ByteArrayInputStream(fileContentAsText.toByteArray(StandardCharsets.UTF_8))
        }
    }
}
