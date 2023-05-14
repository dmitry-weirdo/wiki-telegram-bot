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

        val update = BotTestUtils.getUpdate()

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

        // tabs
        val getTabConfigs = GetTabConfigs()
        val setTabConfigs = SetTabConfigs()

        // statistics
        val getStatistics = GetStatistics()
        val getSuccessfulRequests = GetSuccessfulRequests()
        val getFailedRequests = GetFailedRequests()
        val clearSuccessfulRequests = ClearSuccessfulRequests()
        val clearFailedRequests = ClearFailedRequests()
        val getLastMessageLog = GetLastMessageLog()

        // all bots commands
        val allBotsList = AllBotsList()
        val allBotsReloadFromGoogleSheet = AllBotsReloadFromGoogleSheet()
        val allBotsGetTabConfigs = AllBotsGetTabConfigs()
        val allBotsGetStatistics = AllBotsGetStatistics()
        val allBotsGetSuccessfulRequests = AllBotsGetSuccessfulRequests()
        val allBotsGetFailedRequests = AllBotsGetFailedRequests()

        // Telegram API wrappers
        val getChatInfo = GetChatInfo()
        val getUserInfo = GetUserInfo()

        // execute /helpCommand /listSettings
        val listCommandsResult = wikiBot.processMessage(
            "$botName ${listCommands.defaultCommandName}",
            botAdmin,
            update
        )

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
                + "\n\n— `${wikiBot.botName} ${getTabConfigs.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${setTabConfigs.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getStatistics.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getSuccessfulRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getFailedRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${clearSuccessfulRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${clearFailedRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getLastMessageLog.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${allBotsList.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${allBotsReloadFromGoogleSheet.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${allBotsGetTabConfigs.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${allBotsGetStatistics.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${allBotsGetSuccessfulRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${allBotsGetFailedRequests.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getChatInfo.defaultCommandName}`"
                + "\n\n— `${wikiBot.botName} ${getUserInfo.defaultCommandName}`"
                + "\n\nДля получения справки по команде используйте команду"
                + "\n`${wikiBot.botName} ${helpCommand.defaultCommandName} <commandName>`",
            true
        )

        Assertions.assertThat(listCommandsResult).isEqualTo(expectedListCommandsResult)
    }
}
