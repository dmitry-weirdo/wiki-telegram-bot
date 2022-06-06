package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.exception.CommandException
import com.dv.telegram.notion.NotionCityChats
import org.apache.logging.log4j.kotlin.Logging

class CityChatsValidate : BasicBotCommand(), Logging {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText` — проверить формат списка чатов городов в Google Sheet.
        
        Выдастся либо список ошибок, либо сообщение об успешной проверке.
        
        Эта команда *НЕ* перезагружает список чатов в боте. Для перезагрузки конфига бота используйте `${bot.botName} ${bot.specialCommands.reloadFromGoogleSheetCommand.commandText}`.
        """.trimIndent()

    override val defaultCommandName = "/cityChatsValidate"

    override fun getResponse(text: String, bot: WikiBot): String {
        return try {
            val botData = bot.loadBotDataFromGoogleSheet()

            val cityChats = NotionCityChats.from(botData.cityChats)
            val totalChats = NotionCityChats.countTotalChats(cityChats)

            logger.info("$totalChats city chats for ${cityChats.size} cities successfully parsed from Google Sheet.")

            "$totalChats чатов для ${cityChats.size} городов успешно считаны из Google Sheet."
        }
        catch (e: CommandException) {
            errorResponse(e)
        }
        catch (e: Exception) {
            unknownErrorResponse(e)
        }
    }
}
