package com.dv.telegram;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

class BotNameRegexTest {

    @Test
    void testBotNameRegex() {
        String botName = "Ника";

        Pattern pattern = WikiBot.getBotNameFullWordPattern(botName);

        String s1 = "Привет, НикА дорогая!"; // true
        String s2 = "КлубНика от купальНика."; // false
        String s3 = "Эх ты моя любимая ника, ну ты даёшь!"; // true
        String s4 = "строка раз\nника, работа"; // true
        String s5 = "строка раз\nАлло, ника, работа"; // true
        String s6 = "строка раз\nАлло, вероника, работа"; // false

        assertMatches(pattern, s1);
        assertDoesNotMatch(pattern, s2);
        assertMatches(pattern, s3);
        assertMatches(pattern, s4);
        assertMatches(pattern, s5);
        assertDoesNotMatch(pattern, s6);
    }

    private void assertMatches(Pattern pattern, String s) {
        Assertions.assertThat(
            pattern.matcher(s).matches()
        ).isTrue();
    }

    private void assertDoesNotMatch(Pattern pattern, String s) {
        Assertions.assertThat(
            pattern.matcher(s).matches()
        ).isFalse();
    }
}
