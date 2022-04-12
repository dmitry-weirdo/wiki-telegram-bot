package com.dv.telegram;

import com.dv.telegram.command.BotSpecialCommands;
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

    private final BotSpecialCommands specialCommands;

    private final BotSettings settings; // settings cache in memory

    private final BotStatistics statistics;

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

    public String getBotName() {
        return botName;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public BotSettings getSettings() {
        return settings;
    }

    public BotStatistics getStatistics() {
        return statistics;
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
        Optional<String> specialCommandResponseOptional = specialCommands.getResponse(text, this);
        if (specialCommandResponseOptional.isPresent()) { // special command received -> return response for the special command
            statistics.specialCommandsCount++;
            return specialCommandResponseOptional;
        }

        // todo: nicer straightforward success/no success code instead of ++/-- on "no answer"
        statistics.successfulRequestsCount++;

        // normal commands - configured in the Google Sheet
        List<WikiBotCommandData> matchingCommands = findMatchingCommands(lowerText);
        if (!matchingCommands.isEmpty()) { // matching command found -> only handle the command
            String commandAnswerText = getCommandAnswerText(matchingCommands);
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

    public boolean reloadBotDataFromGoogleSheet() {
        try {
            log.info("Reloading bot data from the Google Sheet...");
            GoogleSheetBotData botData = GoogleSheetLoader.readGoogleSheet(config);
            this.pages = botData.getPages();
            this.cityChats = botData.getCityChats();
            this.commands = botData.getCommands();
            log.info("Bot data successfully reloaded from the Google Sheet.");

            return true;
        }
        catch (Exception e) {
            log.error("Error when loading bot data from the Google Sheet", e);
            return false;
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

    private String getCommandAnswerText(List<WikiBotCommandData> matchingCommands) {
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
