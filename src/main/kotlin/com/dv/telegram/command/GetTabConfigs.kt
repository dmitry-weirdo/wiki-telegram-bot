package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.util.JacksonUtils
import org.telegram.telegrambots.meta.api.objects.Update

class GetTabConfigs : BasicBotCommand() {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) =
        "`${bot.botName} $commandText` — вывести конфигурацию вкладок Google Sheet для текущего бота."

    override fun useMarkdownInResponse() = true

    override val defaultCommandName = "/getTabConfigs"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        val tabConfigsString = JacksonUtils.serializeToString(bot.tabConfigs, true)

        // ugly formatting required for no indents in the response
        return """
Конфигурация вкладок бота:
```
$tabConfigsString```
        """.trimIndent()
    }
}
