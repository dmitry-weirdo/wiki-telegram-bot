package com.dv.telegram.data;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class WikiBotCommandData {
    private String answer;
    private String wordsString;
    private List<String> words;

    public WikiBotCommandData(String answer, String wordsString, List<String> words) { // todo: Kotlin + Lombok hack
        this.answer = answer;
        this.wordsString = wordsString;
        this.words = words;
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

    public String getOneLineAnswer() {
        return answer;
    }

    public String getMultiLineAnswer() {
        return String.format("— %s", answer);
    }
}
