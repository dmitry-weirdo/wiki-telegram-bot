package com.dv.telegram.command

import com.dv.telegram.BotTestUtils
import com.dv.telegram.MessageProcessingResult
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class BotListCommandsTest {

    @Test
    @DisplayName("Test /listCommands command.")
    fun testHelpCommand() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        // order of commands defined by BotCommands.getAllCommands
        val helpCommand = HelpCommand()
        val listCommands = ListCommands()
        val listAdmins = ListAdmins()

        // basic commands
        val start = Start()
        val getEnvironment = GetEnvironment()
        val reloadFromGoogleSheet = ReloadFromGoogleSheet()

        // Notion
        val cityChatsValidate = CityChatsValidate()
        val cityChatsExportToNotion = CityChatsExportToNotion()

        // settings
        val listSettings = ListSettings()
        val helpSetting = HelpSetting()
        val getSetting = GetSetting()
        val setSetting = SetSetting()

        // statistics
        val getStatistics = GetStatistics()
        val getFailedRequests = GetFailedRequests()
        val clearFailedRequests = ClearFailedRequests()

        // all bots commands
        val allBotsList = AllBotsList()

        // execute /helpCommand /listSettings
        val listCommandsResult = wikiBot.processMessage("$botName ${listCommands.defaultCommandName}", botAdmin)

        val expectedListCommandsResult = MessageProcessingResult.specialCommand(
            "Список команд бота:"
                + "\n\n— `${wikiBot.botName} ${helpCommand.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${listCommands.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${listAdmins.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${start.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getEnvironment.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${reloadFromGoogleSheet.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${cityChatsValidate.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${cityChatsExportToNotion.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${listSettings.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${helpSetting.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getSetting.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${setSetting.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getStatistics.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getFailedRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${clearFailedRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${allBotsList.defaultCommandName}`"
                + "\n\nДля получения справки по команде используйте команду"
                + "\n`${wikiBot.botName} ${helpCommand.defaultCommandName} <commandName>`",
            true
        )

        Assertions.assertThat(listCommandsResult).isEqualTo(expectedListCommandsResult)
    }
}
