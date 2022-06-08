package com.dv.telegram.command

import com.dv.telegram.BotTestUtils
import com.dv.telegram.MessageProcessingResult
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class BotHelpCommandsTest {

    @Test
    @DisplayName("Test /helpCommand command: correct command name, successful path.")
    fun testHelpCommand() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val listSettings = ListSettings()
        val helpCommand = HelpCommand()

        // execute /helpCommand /listSettings
        val helpCommandResult = wikiBot.processMessage("$botName ${helpCommand.defaultCommandName} ${listSettings.defaultCommandName}", botAdmin)

        val expectedHelpCommandResult = MessageProcessingResult.specialCommand(
            "*${listSettings.defaultCommandName}*"
                + "\n`${wikiBot.botName} ${listSettings.defaultCommandName}` — вывести список изменяемых в рантайме настроек бота.",
            true
        )

        Assertions.assertThat(helpCommandResult).isEqualTo(expectedHelpCommandResult)
    }

    @Test
    @DisplayName("Test /helpCommand command: incorrect command names.")
    fun testHelpCommandIncorrectCommandNames() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val helpCommand = HelpCommand()

        // no command name
        val noCommandNameResult = wikiBot.processMessage("$botName ${helpCommand.defaultCommandName}", botAdmin)

        val expectedNoCommandNameResult = MessageProcessingResult.specialCommand("Неизвестное имя команды.", true)

        Assertions.assertThat(noCommandNameResult).isEqualTo(expectedNoCommandNameResult)

        // incorrect command name
        val incorrectCommandNameResult = wikiBot.processMessage("$botName ${helpCommand.defaultCommandName} BadCommand", botAdmin)

        val expectedIncorrectCommandNameResult = MessageProcessingResult.specialCommand("Неизвестное имя команды.", true)

        Assertions.assertThat(incorrectCommandNameResult).isEqualTo(expectedIncorrectCommandNameResult)
    }
}
