package com.dv.telegram

import com.dv.telegram.config.BotTriggerMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

internal class BotTriggerModeTest {

    @Test
    @DisplayName("Test bot trigger with BotTriggerMode == ANY_SUBSTRING")
    fun testBotTriggerModeAnySubstring() {
        val wikiBot = getWikiBot(BotTriggerMode.Mode.ANY_SUBSTRING)

        val botName = wikiBot.botName.uppercase() // match must be case-insensitive

        expectNoResultFound(wikiBot, "${botName}, bad command") // trigger
        expectNoResultFound(wikiBot, "${botName}ABC, bad command") // trigger
        expectNoResultFound(wikiBot, "ABC${botName}, bad command") // trigger
        expectNoResultFound(wikiBot, "Hallo, ${botName}, bad command") // trigger
        expectMessageIsNotForTheBot(wikiBot, "NotBotName, bad command") // no trigger
    }

    @Test
    @DisplayName("Test bot trigger with BotTriggerMode == STRING_START")
    fun testBotTriggerModeStringStart() {
        val wikiBot = getWikiBot(BotTriggerMode.Mode.STRING_START)

        val botName = wikiBot.botName.uppercase() // match must be case-insensitive

        expectNoResultFound(wikiBot, "${botName}, bad command") // trigger
        expectNoResultFound(wikiBot, "${botName}ABC, bad command") // trigger
        expectMessageIsNotForTheBot(wikiBot, "ABC${botName}, bad command") // no trigger
        expectMessageIsNotForTheBot(wikiBot, "Hallo, ${botName}, bad command") // no trigger
        expectMessageIsNotForTheBot(wikiBot, "NotBotName, bad command") // no trigger
    }

    @Test
    @DisplayName("Test bot trigger with BotTriggerMode == FULL_WORD")
    fun testBotTriggerModeFullWord() {
        val wikiBot = getWikiBot(BotTriggerMode.Mode.FULL_WORD)

        val botName = wikiBot.botName.uppercase() // match must be case-insensitive

        expectNoResultFound(wikiBot, "${botName}, bad command") // trigger
        expectMessageIsNotForTheBot(wikiBot, "${botName}ABC, bad command") // no trigger
        expectMessageIsNotForTheBot(wikiBot, "ABC${botName}, bad command") // no trigger
        expectNoResultFound(wikiBot, "Hallo, ${botName}, bad command") // trigger
        expectMessageIsNotForTheBot(wikiBot, "NotBotName, bad command") // no trigger
    }

    private fun getWikiBot(mode: BotTriggerMode.Mode): WikiBot {
        return BotTestUtils.getWikiBot {
            it.settings[BotTriggerMode.NAME] = mode.name
            it
        }
    }

    private fun expectNoResultFound(wikiBot: WikiBot, text: String) {
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val result = wikiBot.getResponseText(text, notBotAdmin)

        val expectedResult = MessageProcessingResult.answerNotFound(
            Optional.of(wikiBot.getNoResultAnswer(text))
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    private fun expectMessageIsNotForTheBot(wikiBot: WikiBot, text: String) {
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val result = wikiBot.getResponseText(text, notBotAdmin)

        val expectedResult = MessageProcessingResult.notForTheBot()

        assertThat(result).isEqualTo(expectedResult)
    }
}
