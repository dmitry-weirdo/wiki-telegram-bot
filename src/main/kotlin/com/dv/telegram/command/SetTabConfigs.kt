package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.tabs.TabConfigs
import com.dv.telegram.util.JacksonUtils
import org.telegram.telegrambots.meta.api.objects.Update

class SetTabConfigs : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText <tabConfigsJson>` — установить конфигурацию вкладок Google Sheet в значение `<tabConfigsJson>`.
        
        Эта команда *НЕ* перезагружает данные из Google Sheet в боте. Для перезагрузки конфига бота используйте `${bot.botName} ${bot.specialCommands.reloadFromGoogleSheetCommand.commandText}`.
        """.trimIndent()

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/setTabConfigs"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val commandStartIndex = text.indexOf(commandText)
        if (commandStartIndex < 0) {
            return "JSON-конфиг не может быть пустым. Укажите валидный JSON-конфиг."
        }

        val commandEndIndex = commandStartIndex + commandText.length
        if (commandEndIndex >= text.length) {
            return "JSON-конфиг не может быть пустым. Укажите валидный JSON-конфиг."
        }

        val tabConfigsString = text.substring(commandEndIndex).trim()

        if (tabConfigsString.isBlank()) {
            return "JSON-конфиг не может быть пустым. Укажите валидный JSON-конфиг."
        }

        val tabConfigs: TabConfigs

        try {
            tabConfigs = JacksonUtils.parse(tabConfigsString, TabConfigs::class.java)
        }
        catch (e: Exception) {
            return "Ошибка при парсинге JSON-конфига. Проверьте структуру и заполнение полей в вашем конфиге."
        }

        // todo: maybe validate business logic of tabConfigs: showHeader + header, no repeatable tabs, at lease one tab, etc

        bot.tabConfigs = tabConfigs

        val botTabConfigs = JacksonUtils.serializeToString(bot.tabConfigs, true)

        // ugly formatting required for no indents in the response
        return """
Конфигурация вкладок бота установлена в
```
$botTabConfigs
```
Для перезагрузки конфига бота с новой конфигурацией вкладок используйте `${bot.botName} ${bot.specialCommands.reloadFromGoogleSheetCommand.commandText}`.
        """.trimIndent()
    }
}
