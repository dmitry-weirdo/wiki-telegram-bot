package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.WikiBotConfig
import org.telegram.telegrambots.meta.api.objects.Update

class BotSpecialCommands(val botAdmins: Set<String>, val commands: List<BotCommand>) {
    val startCommand: Start = getCommand(commands, Start::class.java)
    val helpCommand: HelpCommand = getCommand(commands, HelpCommand::class.java)
    val listCommands: ListCommands = getCommand(commands, ListCommands::class.java)
    val reloadFromGoogleSheetCommand: ReloadFromGoogleSheet = getCommand(commands, ReloadFromGoogleSheet::class.java)
    val cityChatsValidateCommand: CityChatsValidate = getCommand(commands, CityChatsValidate::class.java)
    val getStatisticsCommand: GetStatistics = getCommand(commands, GetStatistics::class.java)
    val getSuccessfulRequestsCommand: GetSuccessfulRequests = getCommand(commands, GetSuccessfulRequests::class.java)
    val getFailedRequestsCommand: GetFailedRequests = getCommand(commands, GetFailedRequests::class.java)

    companion object {
        @JvmStatic
        fun create(config: WikiBotConfig): BotSpecialCommands {
            val botAdmins = BotCommandUtils.getBotAdmins(config)
            val botCommands = BotCommandUtils.fillCommands(config)

            return BotSpecialCommands(botAdmins, botCommands)
        }

        private fun <T> getCommand(commands: List<BotCommand>, commandClass: Class<T>): T {
            return commands
                .firstOrNull { commandClass.isInstance(it) }
                .let { commandClass.cast(it) }
                ?: error("No ${commandClass.simpleName} in the list of commands.")
        }
    }

    fun getCommand(commandText: String): BotCommand? {
        return commands
            .firstOrNull { it.commandText == commandText }
    }

    fun useMarkdownInResponse(text: String) =
        commands.any {
            it.textContainsCommand(text) && it.useMarkdownInResponse()
        }

    fun getResponse(text: String, userName: String, bot: WikiBot, update: Update): SpecialCommandResponse {
        val userHasBotAdminRights = userHasRightsToExecuteSpecialCommands(userName)

        val command = commands
            .firstOrNull {
                (userHasBotAdminRights || !it.requiresBotAdminRights())
                && it.textContainsCommand(text)
            }
            ?: return SpecialCommandResponse.noResponse() // no rights on command -> return "special command unknown" response

        val response = command.getResponse(text, bot, update)
        val useMarkdownInResponse = command.useMarkdownInResponse()

        return SpecialCommandResponse.withResponse(response, useMarkdownInResponse)
    }

    private fun userHasRightsToExecuteSpecialCommands(userName: String) = botAdmins.contains(userName)
}
