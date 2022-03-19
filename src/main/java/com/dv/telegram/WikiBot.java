package com.dv.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Locale;

public class WikiBot extends TelegramLongPollingBot {

    public static final String WIKI_BOT_TOKEN_ENV_NAME = "WIKI_BOT_TOKEN";

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
            System.out.println("Unknown command received: " + text);
        }
    }

    private String getResponseText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        if (lowerText.contains("список чатов")) {
            return "Список чатов по городам: https://bit.ly/3sVsDTI";
        }

        if (lowerText.contains("машин") || lowerText.contains("автомобил")) {
            return "Автомобили и что с ними делать: https://bit.ly/365Kkaq";
        }

        if (lowerText.contains("wiki") || lowerText.contains("вики")) {
            return "Стартовая страница вики: https://bit.ly/35ZoUM3";
        }

        return null;
    }

    public String getBotUsername() {
        return "dv_wiki_bot";
    }
}
