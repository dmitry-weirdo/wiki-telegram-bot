package com.dv.telegram;

import com.dv.telegram.data.CommandsParser;
import com.dv.telegram.data.WikiPagesParser;
import com.dv.telegram.google.GoogleSheetReader;
import com.dv.telegram.google.WikiBotGoogleSheet;
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

            List<WikiPageData> wikiPagesData = getWikiPagesData(config);
            List<WikiBotCommandData> commandsData = getCommandsData(config);

            WikiBot wikiBot = new WikiBot(config, wikiPagesData, commandsData);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(wikiBot);
            log.info("The bot \"{}\" has started on \"{}\" environment!", wikiBot.getBotUsername(), wikiBot.getEnvironmentName());
        }
        catch (TelegramApiException e) {
            log.error("Error when starting the WikiBot", e);
        }
    }

    private static List<WikiPageData> getWikiPagesData(WikiBotConfig config) {
        try {
            WikiBotGoogleSheet wikiBotGoogleSheet = GoogleSheetReader.readGoogleSheetSafe(config);
            return WikiPagesParser.parseWikiPages(wikiBotGoogleSheet);
        }
        catch (Exception e) {
            log.error("Failed to parse wiki pages from Google Sheet.", e);

            log.warn("Loading wiki pages from the XLSX file");
            return XlsxParser.parseWikiPagesDataSafe();
        }
    }

    private static List<WikiBotCommandData> getCommandsData(WikiBotConfig config) {
        try {
            WikiBotGoogleSheet wikiBotGoogleSheet = GoogleSheetReader.readGoogleSheetSafe(config);
            return CommandsParser.parseWikiBotCommands(wikiBotGoogleSheet);
        }
        catch (Exception e) {
            log.error("Failed to parse commands from Google Sheet.", e);

            // todo: parse from XLS in worst case
            throw new RuntimeException(e);
//            log.warn("Loading commands from the XLSX file");
//            return XlsxParser.parseWikiPagesDataSafe();
        }
    }
}
