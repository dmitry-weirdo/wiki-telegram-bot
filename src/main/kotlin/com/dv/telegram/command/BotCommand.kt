package com.dv.telegram.command

import com.dv.telegram.WikiBot
import com.dv.telegram.exception.CommandException

interface BotCommand {
    val name: String // english name, no spaces. Used to override defaultCommandName with commandName in the config (e.g. "ListSettings": "/overriddenListSettings"

    fun getDescription(bot: WikiBot): String // description, in Russian // todo: must be localized with MessageBundle
    fun useMarkdownInResponse(): Boolean

    val defaultCommandName: String // default command name when it is not overridden by config
    var commandName: String? // overridden command name. Defines abstract getter and setter.

    val commandText: String
        get() = if (commandName.isNullOrBlank()) {
            defaultCommandName
        }
        else {
            commandName!!
        }

    fun requiresBotAdminRights(): Boolean {
        return true
    }

    fun getResponse(text: String, bot: WikiBot): String

    fun textContainsCommand(text: String): Boolean {
        return text.contains(
            commandText
        )
    }

    fun errorResponse(e: CommandException): String {
        val allErrors: MutableList<String> = mutableListOf()

        // no bold markdown in the header, because we can return the user input data in the error messages
        allErrors.add("При выполнении команды возникли следующие ошибки:")
        e.errorMessages.forEach{
            allErrors.add("— $it\n")
        }

        return allErrors.joinToString("\n")
    }

    fun unknownErrorResponse(e: Exception): String {
        return "При выполнении команды произошла неизвестная ошибка"
    }

    companion object {
        fun getAllCommands() = listOf(
            // commands
            HelpCommand(), // first command to be found since it documents the other commands
            ListCommands(),
            ListAdmins(),

            // basic commands
            Start(),
            GetEnvironment(),
            ReloadFromGoogleSheet(),

            // Notion
            CityChatsValidate(),
            CityChatsExportToNotion(),

            // settings
            ListSettings(),
            HelpSetting(),
            GetSetting(),
            SetSetting(),

            // statistics
            GetStatistics(),
            GetFailedRequests(),
            ClearFailedRequests(),

            // all bots context
            AllBotsList()
        )
    }
}
