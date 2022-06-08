package com.dv.telegram.command

import com.dv.telegram.BotTestUtils
import com.dv.telegram.MessageProcessingResult
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class BotListAdminsTest {

    @Test
    @DisplayName("Test /listAdmins command")
    fun testListAdmins() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val listAdmins = ListAdmins()

        val result = wikiBot.processMessage("$botName ${listAdmins.defaultCommandName}", botAdmin)

        val expectedResult = MessageProcessingResult.specialCommand(
            "— @dmitry\\_weirdo"
                + "\n— @DmRodionov", // "_" must be escaped for Markdown
            true
        )

        Assertions.assertThat(result).isEqualTo(expectedResult)
    }
}
