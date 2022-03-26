package com.dv.telegram;

import com.dv.telegram.util.WikiBotUtils;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Log4j2
public class Main {

    public static void main(String[] args) {
        try {
            WikiBotConfig config = WikiBotUtils.readConfig();

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            List<WikiPageData> wikiPagesData = XlsxParser.parseWikiPagesDataSafe();

            WikiBot wikiBot = new WikiBot(config, wikiPagesData);
            botsApi.registerBot(wikiBot);

            log.info("The bot \"{}\" has started on \"{}\" environment!", wikiBot.getBotUsername(), wikiBot.getEnvironmentName());
        }
        catch (TelegramApiException e) {
            log.error("Error when starting the WikiBot", e);
        }
    }
}
