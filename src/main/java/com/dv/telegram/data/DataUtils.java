package com.dv.telegram.data;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class DataUtils {

    private static final String WORDS_SEPARATOR = ",";

    private DataUtils() {
    }

    public static List<String> parseWords(String wordsString) { // use default separator
        return parseWords(wordsString, WORDS_SEPARATOR);
    }

    public static List<String> parseWords(String wordsString, String separator) {
        if (StringUtils.isBlank(wordsString)) {
            return List.of();
        }

        String[] wordsArray = wordsString
            .toLowerCase(Locale.ROOT)
            .split(separator);

        List<String> words = Arrays.asList(wordsArray);
        return words
            .stream()
            .filter(StringUtils::isNotBlank) // prevent empty strings
            .map(s -> s.toLowerCase(Locale.ROOT).trim())
            .toList();
    }
}
