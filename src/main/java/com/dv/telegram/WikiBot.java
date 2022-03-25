package com.dv.telegram;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Log4j2
public class WikiBot extends TelegramLongPollingBot {

    public static final String WIKI_BOT_TOKEN_ENV_NAME = "WIKI_BOT_TOKEN";
    public static final String WIKI_BOT_ENVIRONMENT_NAME_ENV_NAME = "WIKI_BOT_ENVIRONMENT_NAME";

    public static final String BOT_NAME = "Дюся";
    public static final String BOT_NAME_LOWER_CASE = BOT_NAME.toLowerCase(Locale.ROOT);

    public static final String ALTERNATIVE_BOT_NAME_LOWER_CASE = "боот";

    private final List<WikiPageData> pages;
    private final String environmentName;

    public WikiBot(List<WikiPageData> wikiPagesData) {
        super();

        this.pages = wikiPagesData;
        this.environmentName = getEnvVariable(WIKI_BOT_ENVIRONMENT_NAME_ENV_NAME);
    }

    public String getBotToken() {
        return getEnvVariable(WIKI_BOT_TOKEN_ENV_NAME);
    }

    private static String getEnvVariable(String name) {
        String value = System.getenv(name);
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException(String.format("Environment variable %s is not set.", name));
        }

        return value;
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
        } catch (TelegramApiException e) {
            log.error("TelegramApiException occurred", e); // todo: add which message has failed the bot
            throw new RuntimeException(e);
        }
    }

    private boolean messageIsForTheBot(String lowerText) {
        return lowerText.contains(ALTERNATIVE_BOT_NAME_LOWER_CASE)
            || lowerText.contains(BOT_NAME_LOWER_CASE);
    }

    private Optional<String> getResponseText(String text) {
        if (StringUtils.isBlank(text)) {
            return Optional.empty();
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        if (!messageIsForTheBot(lowerText)) { // only work when bot is mentioned by name
            return Optional.empty();
        }

        Optional<String> specialCommandResponseOptional = handleSpecialCommands(lowerText);
        if (specialCommandResponseOptional.isPresent()) { // special command received -> return response for the special command
            return specialCommandResponseOptional;
        }

        List<WikiPageData> matchingPages = findMatchingPages(lowerText);

        String answerText = getAnswerText(text, matchingPages);
        return Optional.of(answerText);
    }

    private Optional<String> handleSpecialCommands(String text) {
        if (text.contains("ты где") || text.contains("где ты")) {
            String response = String.format("%s живёт здесь: %s.", BOT_NAME, getEnvironmentName());
            return Optional.of(response);
        }

        return Optional.empty();
    }

    private List<WikiPageData> findMatchingPages(String text) {
        return pages
            .stream()
            .filter(page -> page.isPresentIn(text))
            .toList();
    }

    private String getAnswerText(String text, List<WikiPageData> matchingPages) {
        if (matchingPages.isEmpty()) {
            log.info("Unknown command for the bot: {}", text);
            return getNoResultAnswer(text);
        }

        if (matchingPages.size() == 1) {
            return matchingPages.get(0).getOneLineAnswer();
        }

        List<String> multilineAnswers = matchingPages
            .stream()
            .map(WikiPageData::getMultiLineAnswer)
            .toList();

        return StringUtils.join(multilineAnswers, "\n");
    }

    private String getNoResultAnswer(String text) { // todo: think about redirecting to the root wiki page
        return String.format("%s ничего не знает про ваш запрос «%s» :(", BOT_NAME, text);
    }

    public String getBotUsername() {
        return "dv_wiki_bot";
    }

    public String getEnvironmentName() {
        return environmentName;
    }
}
