package com.dv.telegram.command;

import com.dv.telegram.CityChatData;
import com.dv.telegram.GoogleSheetBotData;
import com.dv.telegram.WikiBot;
import com.dv.telegram.exception.CommandException;
import com.dv.telegram.notion.NotionCityChats;
import com.dv.telegram.notion.NotionCityChatsImportResult;
import com.dv.telegram.notion.NotionWrapper;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class CityChatsExportToNotion extends BasicBotCommand {

    @Override
    public String getName() {
        return CityChatsExportToNotion.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            """
            `%s %s` — загрузить список чатов городов из Google Sheet в страницу вики.
            
            Сначала будет выполнена валидация данных чатов в GoogleSheet, аналогичная операции `%s %s`.
            
            Если валидация данных не прошла успешно, импорт в вики выполняться не будет.
            
            Предыдущий список чатов на странице вики будет удалён.

            ❗️Операция импорта в вики занимает длительное время, поэтому не волнуйтесь, что ответ на команду придёт только через несколько минут.
            Вызов команды будет блокировать другие вызовы бота на время её выполнения.
            Параллельные вызовы операции _в одном боте_ будут выполняться один за другим, поэтому обязательно дожидайтесь ответа бота на предыдущий вызов.
            Параллельные вызовы операции _из разных ботов_ будут возвращать ошибку.
            
            Эта команда *НЕ* перезагружает список чатов в боте. Для перезагрузки конфига бота используйте `%s %s`.
            """,
            bot.getBotName(),
            getCommandText() ,
            bot.getBotName(),
            bot.getSpecialCommands().getCityChatsValidateCommand().getCommandText(),
            bot.getBotName(),
            bot.getSpecialCommands().getReloadFromGoogleSheetCommand().getCommandText()
        );
    }

    @Override
    public String getDefaultCommandName() {
        return "/cityChatsExportToNotion";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        try {
            GoogleSheetBotData botData = bot.loadBotDataFromGoogleSheet();
            List<CityChatData> cityChatData = botData.getCityChats();

            List<NotionCityChats> cityChats = NotionCityChats.from(cityChatData);
            int totalChats = NotionCityChats.countTotalChats(cityChats);

            log.info("{} city chats for {} cities successfully parsed from Google Sheet.", totalChats, cityChats.size());

            NotionCityChatsImportResult result = NotionWrapper.appendCityChats(
                bot.getNotionToken(),
                bot.getNotionCityPageId(),
                bot.getNotionCityChatsToggleHeading1Text(),
                cityChats
            );

            return String.format(
                "%d чатов для %d городов успешно считаны из Google Sheet и записаны на страницу вики \"%s\" в секцию \"%s\".",
                result.totalChats,
                result.totalCities,
                result.pageTitle,
                result.toggleHeading1Text
            );
        }
        catch (CommandException e) {
            return errorResponse(e);
        }
        catch (Exception e) {
            return unknownErrorResponse(e);
        }
    }
}
