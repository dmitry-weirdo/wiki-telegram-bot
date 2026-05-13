package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.WikiBotConfig
import com.dv.telegram.exception.CommandException
import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.meta.api.objects.Update

class BotSpecialCommands(val botAdmins: Set<String>, val commands: List<BotCommand>) : Logging {
    val startCommand: Start = getCommand(commands, Start::class.java)
    val helpCommand: HelpCommand = getCommand(commands, HelpCommand::class.java)
    val listCommands: ListCommands = getCommand(commands, ListCommands::class.java)
    val getTabConfigsCommand: GetTabConfigs = getCommand(commands, GetTabConfigs::class.java)
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

        val context = BotContext()

        val response = command.getResponse(text, bot, update, context)
        val useMarkdownInResponse = command.useMarkdownInResponse()

        return if (command.returnFileInResponse()) { // only trigger file-related returns from command if it returns a file
            val responseFileContent = try {
                command.getFileContent(text, bot, update, context)
                    ?: error("Command ${command.javaClass.simpleName} has returnFileInResponse == true, but getFileContent returned null.")
            }
            catch (e: CommandException) { // handle exception gracefully - return it in the response
                logger.error("Error on executing command ${command.javaClass.simpleName}", e)

                val errorMessage: String = e.message ?: "Произошла неизвестная ошибка."

                return SpecialCommandResponse.withResponse(errorMessage, e.useMarkdownInResponse)
            }

            SpecialCommandResponse.withResponse(
                response,
                useMarkdownInResponse,
                command.returnFileInResponse(),
                command.getResponseFileName(context),
                command.getResponseFileCaption(context),
                responseFileContent
            )
        }
        else {
            SpecialCommandResponse.withResponse(response, useMarkdownInResponse)
        }
    }

    private fun userHasRightsToExecuteSpecialCommands(userName: String) = botAdmins.contains(userName)
}
