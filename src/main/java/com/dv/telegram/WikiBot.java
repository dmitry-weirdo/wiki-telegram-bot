package com.dv.telegram;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Log4j2
public class WikiBot extends TelegramLongPollingBot {

    private static final String ALTERNATIVE_BOT_NAME_LOWER_CASE = "боот";
    private static final String START_COMMAND = "/start";

    private final WikiBotConfig config;
    private List<WikiPageData> pages;
    private List<CityChatData> cityChats;
    private List<WikiBotCommandData> commands;

    private final String botName;
    private final String botNameLowerCase;
    private final String environmentName;
    private final String reloadFromGoogleSheetCommandLowerCase;
    private final String startMessage;

    public WikiBot(
        WikiBotConfig config,
        List<WikiPageData> wikiPagesData,
        List<CityChatData> cityChatsData,
        List<WikiBotCommandData> commands
    ) {
        super();

        this.config = config;
        this.pages = wikiPagesData;
        this.cityChats = cityChatsData;
        this.commands = commands;

        this.botName = config.getBotName();
        this.botNameLowerCase = config.getBotName().toLowerCase(Locale.ROOT);
        this.environmentName = config.getEnvironmentName();
        this.reloadFromGoogleSheetCommandLowerCase = config.reloadFromGoogleSheetCommand.toLowerCase(Locale.ROOT);
        this.startMessage = config.getStartMessage();
    }

    public String getBotToken() {
        return config.getBotToken();
    }

    public String getBotUsername() {
        return "dv_wiki_bot"; // todo: read from config if needed. Seems to be overridden by
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void onUpdateReceived(Update update) {
        // todo: env option to disable the bot globally!

        Message updateMessage = update.getMessage();
        if (!update.hasMessage() || !updateMessage.hasText()) {
            return;
        }

        String text = updateMessage.getText();
        String chatId = updateMessage.getChatId().toString();

        Optional<String> responseTextOptional = getResponseText(text);
        if (responseTextOptional.isEmpty()) { // command is not for the bot
            return;
        }

        // command is for the bot -> send the answer
        String responseText = responseTextOptional.get();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(responseText);

        sendMessage.disableWebPagePreview();

        sendMessage.setReplyToMessageId(updateMessage.getMessageId());

        try {
            execute(sendMessage); // Call method to send the message
        }
        catch (TelegramApiException e) {
            log.error("TelegramApiException occurred", e); // todo: add which message has failed the bot
            throw new RuntimeException(e);
        }
    }

    private boolean messageIsForTheBot(String lowerText) {
        return lowerText.contains(ALTERNATIVE_BOT_NAME_LOWER_CASE)
            || lowerText.contains(botNameLowerCase)
            || lowerText.equals(START_COMMAND)
        ;
    }

    private Optional<String> getResponseText(String text) {
        if (StringUtils.isBlank(text)) {
            return Optional.empty();
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        if (!messageIsForTheBot(lowerText)) { // only work when bot is mentioned by name
            return Optional.empty();
        }

        // special commands - not configured in the Google Sheet
        Optional<String> specialCommandResponseOptional = handleSpecialCommands(lowerText);
        if (specialCommandResponseOptional.isPresent()) { // special command received -> return response for the special command
            return specialCommandResponseOptional;
        }

        // normal commands - configured in the Google Sheet
        List<WikiBotCommandData> matchingCommands = findMatchingCommands(lowerText);
        if (!matchingCommands.isEmpty()) { // matching command found -> only handle the command
            String commandAnswerText = getCommandAnswerText(text, matchingCommands);
            return Optional.of(commandAnswerText);
        }

        // wiki pages - configured in the Google Sheet
        List<WikiPageData> matchingPages = findMatchingPages(lowerText);
        Optional<String> wikiPageAnswerText = getWikiPageAnswerText(matchingPages);

        // city pages - configured in the Google Sheet
        List<CityChatData> matchingCityChats = findMatchingCityChats(lowerText);
        Optional<String> cityChatsAnswerText = getCityChatsAnswerText(matchingCityChats);

        if (wikiPageAnswerText.isPresent()) { // wiki pages answer is present
            if (cityChatsAnswerText.isPresent()) { // both wiki pages and city chats present -> return "No result" response
                String combinedAnswers = String.format("%s%n%n%s", wikiPageAnswerText.get(), cityChatsAnswerText.get());
                return Optional.of(combinedAnswers);
            }
            else { // only wiki pages answer is present
                return wikiPageAnswerText;
            }
        }
        else if (cityChatsAnswerText.isPresent()) { // only city chats answer is present
            return cityChatsAnswerText;
        }
        else { // neither wiki pages nor city chats present -> return "No result" response
            log.info("Unknown command for the bot: {}", text);
            String noResultAnswer = getNoResultAnswer(text);
            return Optional.of(noResultAnswer);
        }
    }

    private Optional<String> handleSpecialCommands(String text) {
        if (text.equals(START_COMMAND)) {
            String response = MessageFormat.format(startMessage, botName);
            return Optional.of(response);
        }

        if (text.contains("ты где") || text.contains("где ты")) {
            String response = String.format("%s живёт здесь: %s.", botName, getEnvironmentName());
            return Optional.of(response);
        }

        if (text.contains(reloadFromGoogleSheetCommandLowerCase)) {
            return reloadBotDataFromGoogleSheet();
        }

        return Optional.empty();
    }

    private Optional<String> reloadBotDataFromGoogleSheet() {
        try {
            log.info("Reloading bot data from the Google Sheet...");
            GoogleSheetBotData botData = GoogleSheetLoader.readGoogleSheet(config);
            this.pages = botData.getPages();
            this.cityChats = botData.getCityChats();
            this.commands = botData.getCommands();
            log.info("Bot data successfully reloaded from the Google Sheet.");

            return Optional.of("Данные бота успешно загружены из Google Sheet.");
        }
        catch (Exception e) {
            log.error("Error when loading bot data from the Google Sheet", e);

            return Optional.of("При загрузке данных из Google Sheet произошла ошибка.");
        }
    }

    private List<WikiBotCommandData> findMatchingCommands(String text) {
        return commands
            .stream()
            .filter(command -> command.isPresentIn(text))
            .toList();
    }

    private List<WikiPageData> findMatchingPages(String text) {
        return pages
            .stream()
            .filter(page -> page.isPresentIn(text))
            .toList();
    }

    private List<CityChatData> findMatchingCityChats(String text) {
        return cityChats
            .stream()
            .filter(cityChat -> cityChat.isPresentIn(text))
            .toList();
    }

    private String getCommandAnswerText(String text, List<WikiBotCommandData> matchingCommands) {
        if (matchingCommands.isEmpty()) {
            log.info("Unknown command for the bot: {}", text);
            return getNoResultAnswer(text);
        }

        if (matchingCommands.size() == 1) {
            return matchingCommands.get(0).getOneLineAnswer();
        }

        List<String> multilineAnswers = matchingCommands
            .stream()
            .map(WikiBotCommandData::getMultiLineAnswer)
            .toList();

        return StringUtils.join(multilineAnswers, "\n");
    }

    private Optional<String> getWikiPageAnswerText(List<WikiPageData> matchingPages) {
        if (matchingPages.isEmpty()) {
            return Optional.empty();
        }

        if (matchingPages.size() == 1) {
            String oneLineAnswer = matchingPages.get(0).getOneLineAnswer();
            return Optional.of(oneLineAnswer);
        }

        List<String> multilineAnswers = matchingPages
            .stream()
            .map(WikiPageData::getMultiLineAnswer)
            .toList();

        String answer = StringUtils.join(multilineAnswers, "\n");
        return Optional.of(answer);
    }

    private Optional<String> getCityChatsAnswerText(List<CityChatData> matchingCityChats) {
        if (matchingCityChats.isEmpty()) {
            return Optional.empty();
        }

        List<String> multilineAnswers = matchingCityChats
            .stream()
            .map(CityChatData::getChatsAnswer)
            .toList();

        String answer = StringUtils.join(multilineAnswers, "\n\n");
        return Optional.of(answer);
    }

    private String getNoResultAnswer(String text) { // todo: think about redirecting to the root wiki page
        return String.format("%s ничего не знает про ваш запрос «%s» :(", botName, text);
    }
}
