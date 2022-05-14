package com.dv.telegram

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

internal class BotNameRegexTest {

    @Test
    fun testBotNameRegex() {
        val botName = "Ника"

        val pattern = WikiBot.getBotNameFullWordPattern(botName)

        val s1 = "Привет, НикА дорогая!" // true
        val s2 = "КлубНика от купальНика." // false
        val s3 = "Эх ты моя любимая ника, ну ты даёшь!" // true
        val s4 = "строка раз\nника, работа" // true
        val s5 = "строка раз\nАлло, ника, работа" // true
        val s6 = "строка раз\nАлло, вероника, работа" // false

        assertMatches(pattern, s1)
        assertDoesNotMatch(pattern, s2)
        assertMatches(pattern, s3)
        assertMatches(pattern, s4)
        assertMatches(pattern, s5)
        assertDoesNotMatch(pattern, s6)
    }

    private fun assertMatches(pattern: Pattern, s: String) {
        Assertions.assertThat(
            pattern.matcher(s).matches()
        ).isTrue
    }

    private fun assertDoesNotMatch(pattern: Pattern, s: String) {
        Assertions.assertThat(
            pattern.matcher(s).matches()
        ).isFalse
    }
}
