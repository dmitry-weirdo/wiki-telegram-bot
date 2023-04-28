package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.exception.CommandException
import com.dv.telegram.notion.NotionCityChats
import com.dv.telegram.notion.NotionWrapper
import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.meta.api.objects.Update

class CityChatsExportToNotion : BasicBotCommand(), Logging {
    override val name: String = javaClass.simpleName

    override fun getDescription(bot: WikiBot) = """
        `${bot.botName} $commandText` — загрузить список чатов городов из Google Sheet в страницу вики.
        
        Сначала будет выполнена валидация данных чатов в GoogleSheet, аналогичная операции `${bot.botName} ${bot.specialCommands.cityChatsValidateCommand.commandText}`.
        
        Если валидация данных не прошла успешно, импорт в вики выполняться не будет.
        
        Предыдущий список чатов на странице вики будет удалён.

        ❗️Операция импорта в вики занимает длительное время, поэтому не волнуйтесь, что ответ на команду придёт только через несколько минут.
        Вызов команды будет блокировать другие вызовы бота на время её выполнения.
        Параллельные вызовы операции _в одном боте_ будут выполняться один за другим, поэтому обязательно дожидайтесь ответа бота на предыдущий вызов.
        Параллельные вызовы операции _из разных ботов_ будут возвращать ошибку.
        
        Эта команда *НЕ* перезагружает список чатов в боте. Для перезагрузки конфига бота используйте `${bot.botName} ${bot.specialCommands.reloadFromGoogleSheetCommand.commandText}`.
        """.trimIndent()

    override val defaultCommandName = "/cityChatsExportToNotion"

    override fun getResponse(text: String, bot: WikiBot, update: Update): String {
        return try {
            val botData = bot.loadBotDataFromGoogleSheet()

            val cityChats = NotionCityChats.from(botData.cityChats)
            val totalChats = NotionCityChats.countTotalChats(cityChats)

            logger.info("$totalChats city chats for ${cityChats.size} cities successfully parsed from Google Sheet.")

            val result = NotionWrapper.appendCityChats(
                bot.notionToken,
                bot.notionCityPageId,
                bot.notionCityChatsToggleHeading1Text,
                cityChats
            )

            "${result.totalChats} чатов для ${result.totalCities} городов успешно считаны из Google Sheet и записаны на страницу вики \"${result.pageTitle}\" в секцию \"${result.toggleHeading1Text}\"."
        }
        catch (e: CommandException) {
            errorResponse(e)
        }
        catch (e: Exception) {
            unknownErrorResponse(e)
        }
    }
}
