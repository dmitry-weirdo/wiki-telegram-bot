package com.dv.telegram;

import com.dv.telegram.command.BotSpecialCommands;
import com.dv.telegram.command.SpecialCommandResponse;
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
    private List<CountryChatData> countryChats;
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
        GoogleSheetBotData botData
    ) {
        this(
            config,
            botData.getPages(),
            botData.getCityChats(),
            botData.getCountryChats(),
            botData.getCommands()
        );
    }

    public WikiBot(
        WikiBotConfig config,
        List<WikiPageData> wikiPagesData,
        List<CityChatData> cityChatsData,
        List<CountryChatData> countryChatsData,
        List<WikiBotCommandData> commands
    ) {
        super();

        this.config = config;
        this.pages = wikiPagesData;
        this.cityChats = cityChatsData;
        this.countryChats = countryChatsData;
        this.commands = commands;

        this.botName = config.getBotName();
        this.botNameLowerCase = config.getBotName().toLowerCase(Locale.ROOT);
        this.botNameWordPattern = getBotNameFullWordPattern(botName);

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

    public BotSpecialCommands getSpecialCommands() {
        return specialCommands;
    }

    public BotSettings getSettings() {
        return settings;
    }

    public BotStatistics getStatistics() {
        return statistics;
    }

    public static Pattern getBotNameFullWordPattern(String botName) {
        String botNameRegex = String.format("(?i).*\\b%s\\b.*", botName);
        int botNamePatternFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL;
        return Pattern.compile(botNameRegex, botNamePatternFlags); // see https://stackoverflow.com/a/43738714/8534088
    }

    public void onUpdateReceived(Update update) {
        // todo: env option to disable the bot globally!

        Message updateMessage = update.getMessage();
        if (!update.hasMessage() || !updateMessage.hasText()) {
            return;
        }

        String text = updateMessage.getText();
        String userName = updateMessage.getFrom().getUserName();

        MessageProcessingResult processingResult = getResponseText(text, userName);
        statistics.update(text, processingResult);

        if (!processingResult.messageIsForTheBot) { // message is not for the bot -> do nothing
            return;
        }

        // if the message is a reply, is for the bot and ReplyWhenNoAnswer is false, we still have to delete it
        Message replyToMessage = updateMessage.getReplyToMessage();
        boolean updateMessageIsReply = (replyToMessage != null);

        boolean deleteBotCallMessage = settings.deleteBotCallMessageOnMessageReply && updateMessageIsReply;
        if (deleteBotCallMessage) {
            deleteBotCallMessage(updateMessage);
        }

        if (processingResult.hasNoResponse()) { // no response -> nothing to return
            return;
        }

        // command is for the bot and has the response -> send the response message
        try {
            SendMessage sendMessage = createSendMessage(updateMessage, replyToMessage, deleteBotCallMessage, processingResult);
            execute(sendMessage); // Call method to send the message
        }
        catch (TelegramApiException e) {
            log.error("TelegramApiException occurred on sending the bot response message", e); // todo: add which message has failed the bot
            throw new RuntimeException(e);
        }
    }

    private SendMessage createSendMessage(
        Message updateMessage,
        Message replyToMessage,
        boolean deleteBotCallMessage,
        MessageProcessingResult processingResult
    ) {
        String chatId = updateMessage.getChatId().toString();
        String responseText = processingResult.getResponse();

        Integer replyToMessageId = deleteBotCallMessage
            ? replyToMessage.getMessageId() // reply to the original message
            : updateMessage.getMessageId() // reply to the "call bot" message
        ;

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(replyToMessageId);
        sendMessage.setText(responseText);
        sendMessage.disableWebPagePreview();

        if (processingResult.useMarkdown) {
            sendMessage.setParseMode("Markdown");
        }

        return sendMessage;
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

    private MessageProcessingResult getResponseText(String text, String userName) {
        if (StringUtils.isBlank(text)) {
            return MessageProcessingResult.notForTheBot();
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        if (!messageIsForTheBot(lowerText)) { // only work when bot is mentioned by name
            return MessageProcessingResult.notForTheBot();
        }

        // special commands - not configured in the Google Sheet
        SpecialCommandResponse specialCommandResponse = specialCommands.getResponse(text, userName, this);
        if (specialCommandResponse.hasResponse()) { // special command received -> return response for the special command
            return MessageProcessingResult.specialCommand(specialCommandResponse.response, specialCommandResponse.useMarkdownInResponse);
        }

        // normal commands - configured in the Google Sheet
        List<WikiBotCommandData> matchingCommands = findMatchingCommands(lowerText);
        if (!matchingCommands.isEmpty()) { // matching command found -> only handle the command
            String commandAnswerText = getCommandAnswerText(matchingCommands);

            return MessageProcessingResult.answerFound(commandAnswerText);
        }

        // wiki pages - configured in the Google Sheet
        List<WikiPageData> matchingPages = findMatchingPages(lowerText);
        Optional<String> wikiPageAnswerText = getWikiPageAnswerText(matchingPages);

        // city chats - configured in the Google Sheet
        List<CityChatData> matchingCityChats = findMatchingCityChats(lowerText);
        Optional<String> cityChatsAnswerText = getCityChatsAnswerText(matchingCityChats);

        // country chats - configured in the Google Sheet
        List<CountryChatData> matchingCountryChats = findMatchingCountryChats(lowerText);
        Optional<String> countryChatsAnswerText = getCountryChatsAnswerText(matchingCountryChats);

        List<Optional<String>> answers = List.of(
            wikiPageAnswerText,
            cityChatsAnswerText,
            countryChatsAnswerText
        );

        return getResponseText(text, answers);
    }

    private MessageProcessingResult getResponseText(String text, List<Optional<String>> answerOptionals) {
        List<String> answers = answerOptionals
            .stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        if (answers.isEmpty()) { // no answers found
            Optional<String> noResultResponse = getNoResultResponse(text);
            return MessageProcessingResult.answerNotFound(noResultResponse);
        }

        String combinedAnswers = StringUtils.join(answers, "\n\n");
        return MessageProcessingResult.answerFound(combinedAnswers);
    }

    public GoogleSheetBotData loadBotDataFromGoogleSheet() { // does NOT reload the bot data itself
        return GoogleSheetLoader.readGoogleSheet(config);
    }

    public boolean reloadBotDataFromGoogleSheet() {
        try {
            GoogleSheetBotData botData = GoogleSheetLoader.readGoogleSheet(config);
            this.pages = botData.getPages();
            this.cityChats = botData.getCityChats();
            this.countryChats = botData.getCountryChats();
            this.commands = botData.getCommands();

            return true;
        }
        catch (Exception e) {
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

    private List<CountryChatData> findMatchingCountryChats(String text) {
        return countryChats
            .stream()
            .filter(countryChat -> countryChat.isPresentIn(text))
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

    private Optional<String> getCountryChatsAnswerText(List<CountryChatData> matchingCityChats) {
        if (matchingCityChats.isEmpty()) {
            return Optional.empty();
        }

        List<String> multilineAnswers = matchingCityChats
            .stream()
            .map(CountryChatData::getChatsAnswer)
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
