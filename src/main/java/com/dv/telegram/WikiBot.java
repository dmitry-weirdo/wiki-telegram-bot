package com.dv.telegram;

import com.dv.telegram.command.BasicBotCommand;
import com.dv.telegram.command.BotSpecialCommands;
import com.dv.telegram.config.BotSetting;
import com.dv.telegram.config.BotSettings;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Log4j2
public class WikiBot extends TelegramLongPollingBot {

    private static final String START_COMMAND = "/start";

    private final WikiBotConfig config;
    private List<WikiPageData> pages;
    private List<CityChatData> cityChats;
    private List<WikiBotCommandData> commands;

    private final String botName;
    private final String botNameLowerCase;
    private final Pattern botNameWordPattern;
    private final String environmentName;
    private final String reloadFromGoogleSheetCommandLowerCase;

    private final BotSpecialCommands specialCommands;

    private final BotSettings settings; // settings cache in memory

    private final BotStatistics statistics;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm:ss");

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

        String regex = String.format("(?i).*\\b%s\\b.*", botName);
        this.botNameWordPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE); // see https://stackoverflow.com/a/43738714/8534088

        this.environmentName = config.getEnvironmentName();
        this.reloadFromGoogleSheetCommandLowerCase = config.reloadFromGoogleSheetCommand.toLowerCase(Locale.ROOT);

        this.specialCommands = BotSpecialCommands.create(config);
        this.settings = BotSettings.create(config);

        this.statistics = new BotStatistics();
    }

    public String getBotToken() {
        return config.getBotToken();
    }

    public String getBotUsername() {
        return "dv_wiki_bot"; // todo: read from config if needed. Seems to be overridden by what is set by "/setname" in the @BotFather
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
        if (responseTextOptional.isEmpty()) { // command is not for the bot or there is no answer and ReplyWhenNoAnswer is false
            return;
        }

        Message replyToMessage = updateMessage.getReplyToMessage();
        boolean updateMessageIsReply = (replyToMessage != null);

        boolean deleteBotCallMessage = settings.deleteBotCallMessageOnMessageReply && updateMessageIsReply;
        if (deleteBotCallMessage) {
            deleteBotCallMessage(updateMessage);
        }

        // command is for the bot -> send the answer
        String responseText = responseTextOptional.get();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(responseText);

        if (useMarkdown(text)) {
            sendMessage.setParseMode("Markdown");
        }

        sendMessage.disableWebPagePreview();

        Integer replyToMessageId = deleteBotCallMessage
            ? replyToMessage.getMessageId() // reply to the original message
            : updateMessage.getMessageId() // reply to the "call bot" message
        ;

        sendMessage.setReplyToMessageId(replyToMessageId);

        try {
            execute(sendMessage); // Call method to send the message
        }
        catch (TelegramApiException e) {
            log.error("TelegramApiException occurred", e); // todo: add which message has failed the bot
            throw new RuntimeException(e);
        }
    }

    private void deleteBotCallMessage(Message updateMessage) {
        DeleteMessage deleteMessage = new DeleteMessage(
            updateMessage.getChatId().toString(),
            updateMessage.getMessageId()
        );

        try {
            execute(deleteMessage);
        }
        catch (TelegramApiException e) {
            log.error("Failed to delete message", e);
        }
    }

    private boolean messageIsForTheBot(String lowerText) {
        if (lowerText.equals(START_COMMAND)) { // special case: /start command without bot name
            return true;
        }

        return switch (settings.triggerMode) { // check whether the message contains the bot name
            case ANY_SUBSTRING -> lowerText.contains(botNameLowerCase);
            case STRING_START -> lowerText.startsWith(botNameLowerCase);
            case FULL_WORD -> botNameWordPattern.matcher(lowerText).matches();
        };
    }

    private boolean useMarkdown(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }

        String lowerText = text.toLowerCase(Locale.ROOT).trim();

        if (!messageIsForTheBot(lowerText)) {
            return false;
        }

        return specialCommands.useMarkdownInResponse(text);

        // todo: use markdown: true in other special commands
/*
        return text.contains(config.listSettingsCommand)
            || text.contains(config.helpSettingCommand)
            || text.contains(config.getSettingCommand)
            || text.contains(config.setSettingCommand);
*/
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
        Optional<String> specialCommandResponseOptional = handleSpecialCommands(text, lowerText);
        if (specialCommandResponseOptional.isPresent()) { // special command received -> return response for the special command
            statistics.specialCommandsCount++;
            return specialCommandResponseOptional;
        }

        // todo: nicer straightforward success/no success code instead of ++/-- on "no answer"
        statistics.successfulRequestsCount++;

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
            statistics.successfulRequestsCount--;
            statistics.failedRequestsCount++;
            statistics.addFailedRequest(text);

            return getNoResultResponse(text);
        }
    }

    private Optional<String> handleSpecialCommands(String text, String lowerText) {
        // todo: make this a nicer code instead a chain of "ifs"

        // todo: start is also a special command!
        if (lowerText.equals(START_COMMAND)) {
            String response = MessageFormat.format(settings.startMessage, botName);
            return Optional.of(response);
        }

        Optional<String> specialCommandResponse = specialCommands.getResponse(text, settings);
        if (specialCommandResponse.isPresent()) { // todo: after all commands moved, just return specialCommandResponse
            return specialCommandResponse;
        }

        if (text.contains(config.helpSettingCommand)) {
            String response = getHelpSettingResponse(text);
            return Optional.of(response);
        }

        if (text.contains(config.getSettingCommand)) {
            String response = getGetSettingResponse(text);
            return Optional.of(response);
        }

        if (text.contains(config.setSettingCommand)) {
            String response = getSetSettingResponse(text);
            return Optional.of(response);
        }

        if (text.contains(config.getStatisticsCommand)) {
            String response = getGetStatisticsResponse();
            return Optional.of(response);
        }

        if (text.contains(config.getFailedRequestsCommand)) {
            String response = getGetFailedRequestsResponse();
            return Optional.of(response);
        }

        if (text.contains(config.clearFailedRequestsCommand)) {
            String response = getClearFailedRequestsResponse();
            return Optional.of(response);
        }

        if (lowerText.contains("ты где") || lowerText.contains("где ты")) { // todo: extract to config commands
            String response = String.format("%s живёт здесь: %s.", botName, getEnvironmentName());
            return Optional.of(response);
        }

        if (lowerText.contains(reloadFromGoogleSheetCommandLowerCase)) {
            return reloadBotDataFromGoogleSheet();
        }

        return Optional.empty();
    }

    private String getHelpSettingResponse(String text) {
        int commandStartIndex = text.indexOf(config.helpSettingCommand);
        if (commandStartIndex < 0) {
            return unknownSettingResponse();
        }

        int commandEndIndex = commandStartIndex + config.helpSettingCommand.length();
        if (commandEndIndex >= text.length()) {
            return unknownSettingResponse();
        }

        String settingName = text.substring(commandEndIndex).trim();

        BotSetting<?> botSetting = settings.getBotSetting(settingName);
        if (botSetting == null) {
            return unknownSettingResponse();
        }

        return String.format("*%s*%n%s", botSetting.getName(), botSetting.getDescription());
    }

    private String getGetSettingResponse(String text) {
        int commandStartIndex = text.indexOf(config.getSettingCommand);
        if (commandStartIndex < 0) {
            return unknownSettingResponse();
        }

        int commandEndIndex = commandStartIndex + config.getSettingCommand.length();
        if (commandEndIndex >= text.length()) {
            return unknownSettingResponse();
        }

        String settingName = text.substring(commandEndIndex).trim();

        BotSetting<?> botSetting = settings.getBotSetting(settingName);
        if (botSetting == null) {
            return unknownSettingResponse();
        }

        String value = BasicBotCommand.getSettingValueForMarkdown(botSetting);
        return String.format("*%s*%n%s", botSetting.getName(), value);
    }

    private String getSetSettingResponse(String text) {
        int commandStartIndex = text.indexOf(config.setSettingCommand);
        if (commandStartIndex < 0) {
            return unknownSettingResponse();
        }

        int commandEndIndex = commandStartIndex + config.setSettingCommand.length();
        if (commandEndIndex >= text.length()) {
            return unknownSettingResponse();
        }

        String settingNameAndValue = text.substring(commandEndIndex).trim();

        String nameValueSeparator = " ";
        int separatorIndex = settingNameAndValue.indexOf(nameValueSeparator);

        if (separatorIndex < 0 || separatorIndex >= settingNameAndValue.length()) {
            return unknownSettingResponse(); // todo: probably another more concrete response
        }

        String settingName = settingNameAndValue.substring(0, separatorIndex).trim();
        String settingValue = settingNameAndValue.substring(separatorIndex).trim();

        BotSetting<?> botSetting = settings.getBotSetting(settingName);
        if (botSetting == null) {
            return unknownSettingResponse();
        }

        try {
            botSetting.setValue(settingValue);
            settings.fillSettingCacheFields();
        }
        catch (Exception e) {
            return String.format("Ошибка при установке настройки *%s* в значение%n%s", settingName, BasicBotCommand.getSettingValueForMarkdown(settingValue));
        }

        return String.format("*%s* установлена в значение%n%s", botSetting.getName(), BasicBotCommand.getSettingValueForMarkdown(botSetting));
    }

    private String unknownSettingResponse() {
        return "Неизвестное имя настройки.";
    }

    private String getGetStatisticsResponse() {
        List<String> statisticsLines = List.of(
            getStatisticsLine("Время старта бота", statistics.startTime),
            getStatisticsLine("Успешных запросов", statistics.getSuccessfulRequestsCountWithPercentage()),
            getStatisticsLine("Неуспешных запросов", statistics.getFailedRequestsCountWithPercentage()),
            getStatisticsLine("Всего запросов", statistics.getTotalCallsWithPercentage()),
            getStatisticsLine("Вызовов специальных команд", statistics.specialCommandsCount),
            getStatisticsLine("Всего запросов (вместе со специальными командами)", statistics.getTotalCallsWithSpecialCommands())
        );

        return StringUtils.join(statisticsLines, "\n");
    }

    private String getStatisticsLine(String name, long count) {
        return String.format("— %s: %d", name, count);
    }

    private String getStatisticsLine(String name, String value) {
        return String.format("— %s: %s", name, value);
    }

    private String getStatisticsLine(String name, ZonedDateTime time) {
        return String.format("— %s: %s", name, dateTimeFormatter.format(time));
    }

    private String getGetFailedRequestsResponse() {
        List<String> failedRequestsLines = statistics
            .getFailedRequests()
            .stream()
            .map(failedRequest -> String.format("— %s", failedRequest))
            .toList();


        String totalLine = String.format("Разных неудачных запросов: %d", failedRequestsLines.size());

        List<String> responseLines = new ArrayList<>();
        responseLines.add(totalLine);
        responseLines.addAll(failedRequestsLines);

        return StringUtils.join(responseLines, "\n");
    }

    private String getClearFailedRequestsResponse() {
        int clearedFailedRequestsCount = statistics.failedRequests.size();
        statistics.clearFailedRequests();
        return String.format("Список из %d неудачных запросов к боту очищен.", clearedFailedRequestsCount);
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

    private Optional<String> getNoResultResponse(String text) {
        log.info("Unknown command for the bot: {}", text);

        if (settings.replyWhenNoAnswer) { // reply on no answer
            String noResultAnswer = getNoResultAnswer(text);
            return Optional.of(noResultAnswer);
        }

        // no reply on no answer
        return Optional.empty();
    }

    private String getNoResultAnswer(String text) {
        return MessageFormat.format(settings.noAnswerReply, botName, text);
    }
}
