package com.dv.telegram;

import com.dv.telegram.config.SettingValidationException;
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
public class MainJava {

    public static void main(String[] args) {
        try {
            WikiBotConfigs wikiBotConfigs = WikiBotUtils.readConfigs();

            int threadsCount = wikiBotConfigs.getConfigs().size();
            log.info("Total bot configs: {}", threadsCount);

            List<Callable<String>> callableTasks = wikiBotConfigs
                .getConfigs()
                .stream()
                .map(MainJava::createCallableTask)
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
                GoogleSheetBotData botData = GoogleSheetLoader.readGoogleSheet(config);

                WikiBot wikiBot = new WikiBot(config, botData);

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
            catch (Exception e) {
                log.error("Unknown exception when starting the WikiBot", e);
                throw new RuntimeException(e);
            }
        };
    }
}
