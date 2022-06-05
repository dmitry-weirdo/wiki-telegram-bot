package com.dv.telegram;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@AllArgsConstructor
public class CountryChatData {
    private String countryName;
    private String wordsString;
    private List<String> words;
    private List<String> chats; // multiple chats possible for the same country are possible

    private String chatsAnswer;

    public CountryChatData(String countryName, String wordsString, List<String> words, List<String> chats) {
        this.countryName = countryName;
        this.wordsString = wordsString;
        this.words = words;
        this.chats = chats;
        fillChatsAnswer(); // pre-fill the bot answer to join the strings only once
    }

    public boolean isPresentIn(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }

        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }

    public void fillChatsAnswer() {
        String chatsList = getChatLines();
        chatsAnswer = String.format("%s чаты:\n%s", countryName, chatsList); // %n will format as system-specific separator, see https://stackoverflow.com/questions/1883345/whats-up-with-javas-n-in-printf
    }

    private String getChatLines() {
        List<String> chatLines = chats
            .stream()
            .map(chat -> String.format("— %s", chat))
            .toList();

        return StringUtils.join(chatLines, "\n");
    }
}
