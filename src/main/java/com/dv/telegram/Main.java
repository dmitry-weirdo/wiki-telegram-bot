package com.dv.telegram;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Log4j2
public class Main {

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            List<WikiPageData> wikiPagesData = XlsxParser.parseWikiPagesDataSafe();

            WikiBot wikiBot = new WikiBot(wikiPagesData);
            botsApi.registerBot(wikiBot);

            log.info("The bot \"{}\" has started!", wikiBot.getBotUsername());
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
