package com.dv.telegram.command

import com.dv.telegram.WikiBotConfig

object BotCommandUtils {
    private const val USER_NAME_PREFIX = "@"

    @JvmStatic
    fun fillCommands(config: WikiBotConfig): List<BotCommand> {
        val allCommands = BotCommand.getAllCommands()

        val commandNames = config.commands
        if (commandNames.isEmpty()) {
            return allCommands
        }

        for (command in allCommands) {
            val name = command.name

            val overriddenCommandName = commandNames[name]
            if (!overriddenCommandName.isNullOrBlank()) {
                command.commandName = overriddenCommandName
            }
        }

        return allCommands
    }

    @JvmStatic
    fun getBotAdmins(config: WikiBotConfig): Set<String> {
        val botAdmins = config.botAdmins

        return botAdmins
            .map { normalizeUserName(it) } // cut off "@" if it is present
            .toSortedSet(Comparator.comparing { it.lowercase() }) // sort admins set by userName, prevent java's "big letters first" sorting
    }

    fun normalizeUserName(userName: String?): String {
        require( !userName.isNullOrBlank() ) { "userName cannot be null or blank." }

        if (userName.startsWith(USER_NAME_PREFIX)) { // cut off "@" if it is present
            return userName.substring(USER_NAME_PREFIX.length)
        }

        return userName
    }

    @JvmStatic
    fun getClickableUserName(userName: String?): String {
        require( !userName.isNullOrBlank() ) { "userName cannot be null or blank." }

        if (userName.startsWith(USER_NAME_PREFIX)) {
            return userName
        }

        // append "@" if it is NOT present
        return "$USER_NAME_PREFIX$userName"
    }
}
