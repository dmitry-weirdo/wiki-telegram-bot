package com.dv.telegram.command;

import com.dv.telegram.GoogleSheetBotData;
import com.dv.telegram.WikiBot;
import com.dv.telegram.data.CityChatData;
import com.dv.telegram.exception.CommandException;
import com.dv.telegram.notion.NotionCityChats;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class CityChatsValidate extends BasicBotCommand {

    @Override
    public String getName() {
        return CityChatsValidate.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            """
            `%s %s` — проверить формат списка чатов городов в Google Sheet.
            
            Выдастся либо список ошибок, либо сообщение об успешной проверке.
            
            Эта команда *НЕ* перезагружает список чатов в боте. Для перезагрузки конфига бота используйте `%s %s`.
            """,
            bot.getBotName(),
            getCommandText(),
            bot.getBotName(),
            bot.getSpecialCommands().getReloadFromGoogleSheetCommand().getCommandText()
        );
    }

    @Override
    public String getDefaultCommandName() {
        return "/cityChatsValidate";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        try {
            GoogleSheetBotData botData = bot.loadBotDataFromGoogleSheet();
            List<CityChatData> cityChatData = botData.getCityChats();

            List<NotionCityChats> cityChats = NotionCityChats.from(cityChatData);
            int totalChats = NotionCityChats.countTotalChats(cityChats);

            log.info("{} city chats for {} cities successfully parsed from Google Sheet.", totalChats, cityChats.size());

            return String.format("%d чатов для %d городов успешно считаны из Google Sheet.", totalChats, cityChats.size());
        }
        catch (CommandException e) {
            return errorResponse(e);
        }
        catch (Exception e) {
            return unknownErrorResponse(e);
        }
    }
}
