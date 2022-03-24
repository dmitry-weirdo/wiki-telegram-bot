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

@Log4j2
public class WikiBot extends TelegramLongPollingBot {

    public static final String WIKI_BOT_TOKEN_ENV_NAME = "WIKI_BOT_TOKEN";

    public static final String BOT_NAME = "Дюся";
    public static final String BOT_NAME_LOWER_CASE = BOT_NAME.toLowerCase(Locale.ROOT);

    public static final String ALTERNATIVE_BOT_NAME_LOWER_CASE = "боот";

    private final List<WikiPageData> pages;

    public WikiBot(List<WikiPageData> wikiPagesData) {
        super();

        this.pages = wikiPagesData;
    }

    public String getBotToken() {
        String token = System.getenv(WIKI_BOT_TOKEN_ENV_NAME);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(String.format("Env variable %s is not set.", WIKI_BOT_TOKEN_ENV_NAME));
        }

        return token;
    }

    public void onUpdateReceived(Update update) {
        // todo: env option to disable the bot globally!

        Message updateMessage = update.getMessage();
        if (!update.hasMessage() || !updateMessage.hasText()) {
            return;
        }

        String text = updateMessage.getText();
        String chatId = updateMessage.getChatId().toString();


        String responseText = getResponseText(text);

        if (responseText != null) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(responseText);

            sendMessage.disableWebPagePreview();

            sendMessage.setReplyToMessageId(updateMessage.getMessageId());

            try {
                execute(sendMessage); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        else {
            log.info("Unknown command received: " + text);
        }
    }

    private String getResponseText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        if (!lowerText.contains(ALTERNATIVE_BOT_NAME_LOWER_CASE) && !lowerText.contains(BOT_NAME_LOWER_CASE)) { // only work when bot is mentioned by name
            return null;
        }

        List<WikiPageData> matchingPages = findMatchingPages(lowerText);

        return getAnswerText(text, matchingPages);
    }

    private List<WikiPageData> findMatchingPages(String text) {
        return pages
            .stream()
            .filter(page -> page.isPresentIn(text))
            .toList();
    }

    private String getAnswerText(String text, List<WikiPageData> matchingPages) {
        if (matchingPages.isEmpty()) {
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
}
