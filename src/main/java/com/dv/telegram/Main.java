package com.dv.telegram;

import com.dv.telegram.config.SettingValidationException;
import com.dv.telegram.data.CityChatsParser;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class Main {

    public static void main(String[] args) {
        try {
            WikiBotConfigs wikiBotConfigs = WikiBotUtils.readConfigs();

            int threadsCount = wikiBotConfigs.getConfigs().size();
            log.info("Total bot configs: {}", threadsCount);

            List<Callable<String>> callableTasks = wikiBotConfigs
                .getConfigs()
                .stream()
                .map(Main::createCallableTask)
                .toList();

            ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
            executorService.invokeAll(callableTasks); // todo: this currently does not stop Main on exception in the thread
        }
        catch (InterruptedException e) {
            log.debug("============================================");
            log.error("executorService.invokeAll was interrupted", e);
            throw new RuntimeException(e);
        }
    }

    private static Callable<String> createCallableTask(WikiBotConfig config) {
        return () -> {
            try {
                List<WikiPageData> wikiPagesData = getWikiPagesData(config);
                List<CityChatData> cityChatsData = getCityChatsData(config);
                List<WikiBotCommandData> commandsData = getCommandsData(config);

                WikiBot wikiBot = new WikiBot(config, wikiPagesData, cityChatsData, commandsData);

                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(wikiBot);
                log.info("The bot \"{}\" has started on \"{}\" environment!", wikiBot.getBotUsername(), wikiBot.getEnvironmentName());
                return String.format("The bot \"%s\" has started on \"%s\" environment!", wikiBot.getBotUsername(), wikiBot.getEnvironmentName());
            }
            catch (TelegramApiException e) {
                log.error("Error when starting the WikiBot", e);
                throw new RuntimeException(e);
            }
            catch (SettingValidationException e) {
                log.error("Settings validation error when starting the WikiBot", e);
                throw new RuntimeException(e);
            }
        };
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

    private static List<CityChatData> getCityChatsData(WikiBotConfig config) {
        try {
            WikiBotGoogleSheet wikiBotGoogleSheet = GoogleSheetReader.readGoogleSheetSafe(config);
            return CityChatsParser.parseCityChats(wikiBotGoogleSheet);
        }
        catch (Exception e) {
            log.error("Failed to parse city chats from Google Sheet.", e);

            // todo: parse from XLS in worst case
            throw new RuntimeException(e);
//            log.warn("Loading commands from the XLSX file");
//            return XlsxParser.parseWikiPagesDataSafe();
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
