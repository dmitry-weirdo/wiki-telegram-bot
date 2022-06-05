package com.dv.telegram;

import com.dv.telegram.command.BotSpecialCommands;
import com.dv.telegram.config.BotSettings;
import com.dv.telegram.data.*;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Log4j2
public class WikiBot extends TelegramLongPollingBot {

    private final WikiBotConfig config;
    private WikiPagesDataList pages;
    private CityChatsDataList cityChats;
    private CountryChatsDataList countryChats;
    private WikiBotCommandsDataList commands;

    private final String botName;

    private final String environmentName;

    private final BotSpecialCommands specialCommands;

    private final BotSettings settings; // settings cache in memory

    private final BotStatistics statistics;

    private final WikiBotMessageProcessor messageProcessor;

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
        this.pages = new WikiPagesDataList(wikiPagesData);
        this.cityChats = new CityChatsDataList(cityChatsData);
        this.countryChats = new CountryChatsDataList(countryChatsData);
        this.commands = new WikiBotCommandsDataList(commands);

        this.botName = config.getBotName();

        this.environmentName = config.getEnvironmentName();

        this.specialCommands = BotSpecialCommands.create(config);
        this.settings = BotSettings.create(config);

        this.statistics = new BotStatistics();

        this.messageProcessor = new WikiBotMessageProcessor(this);
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

    public String getNotionToken() {
        return config.notionToken;
    }

    public String getNotionCityPageId() {
        return config.cityChatsPageId;
    }

    public String getNotionCityChatsToggleHeading1Text() {
        return config.cityChatsToggleHeading1Text;
    }

    public BotSpecialCommands getSpecialCommands() {
        return specialCommands;
    }

    public WikiPagesDataList getPages() {
        return pages;
    }

    public CityChatsDataList getCityChats() {
        return cityChats;
    }

    public CountryChatsDataList getCountryChats() {
        return countryChats;
    }

    public WikiBotCommandsDataList getCommands() {
        return commands;
    }

    public BotSettings getSettings() {
        return settings;
    }

    public BotStatistics getStatistics() {
        return statistics;
    }

    public WikiBotMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public void onUpdateReceived(Update update) {
        // todo: env option to disable the bot globally!

        Message updateMessage = update.getMessage();
        if (!update.hasMessage() || !updateMessage.hasText()) {
            return;
        }

        String text = updateMessage.getText();
        String userName = updateMessage.getFrom().getUserName();

        MessageProcessingResult processingResult = processMessage(text, userName);
        statistics.update(text, processingResult);

        if (!processingResult.getMessageIsForTheBot()) { // message is not for the bot -> do nothing
            return;
        }

        // if the message is a reply, is for the bot and ReplyWhenNoAnswer is false, we still have to delete it
        Message replyToMessage = updateMessage.getReplyToMessage();
        boolean updateMessageIsReply = (replyToMessage != null);

        boolean deleteBotCallMessage = settings.getDeleteBotCallMessageOnMessageReply() && updateMessageIsReply;
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
        String responseText = processingResult.getResponseOrFail();

        Integer replyToMessageId = deleteBotCallMessage
            ? replyToMessage.getMessageId() // reply to the original message
            : updateMessage.getMessageId() // reply to the "call bot" message
        ;

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(replyToMessageId);
        sendMessage.setText(responseText);
        sendMessage.disableWebPagePreview();

        if (processingResult.getUseMarkdown()) {
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

    MessageProcessingResult processMessage(String text, String userName) { // non-private for testing
        return messageProcessor.processMessage(text, userName);
    }

    public GoogleSheetBotData loadBotDataFromGoogleSheet() { // does NOT reload the bot data itself
        return GoogleSheetLoader.readGoogleSheet(config);
    }

    public boolean reloadBotDataFromGoogleSheet() {
        try {
            GoogleSheetBotData botData = loadBotDataFromGoogleSheet();
            this.pages = new WikiPagesDataList(botData.getPages());
            this.cityChats = new CityChatsDataList(botData.getCityChats());
            this.countryChats = new CountryChatsDataList(botData.getCountryChats());
            this.commands = new WikiBotCommandsDataList(botData.getCommands());

            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
