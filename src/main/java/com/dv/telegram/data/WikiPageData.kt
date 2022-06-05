package com.dv.telegram.data;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class WikiPageData {
    private String name;
    private String url;
    private String wordsString;
    private List<String> words;

    public WikiPageData(String name, String url, String wordsString, List<String> words) { // todo: Kotlin + Lombok hack
        this.name = name;
        this.url = url;
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
        return String.format("%s — %s", name, url);
    }

    public String getMultiLineAnswer() {
        return String.format("— %s — %s", name, url);
    }
}
