package com.dv.telegram.command

import com.dv.telegram.WikiBotConfig
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class BotCommandUtilsTest {

    @Test
    fun testNormalizeUserName() {
        // cut @
        Assertions.assertThat(
            BotCommandUtils.normalizeUserName("@dmitry_weirdo")
        )
            .isEqualTo("dmitry_weirdo")

        // no @ -> nothing to cut
        Assertions.assertThat(
            BotCommandUtils.normalizeUserName("dmitry_weirdo")
        )
            .isEqualTo("dmitry_weirdo")

        // blank string must throw
        Assertions.assertThatThrownBy { BotCommandUtils.normalizeUserName("   \t  ") }
            .isInstanceOf(IllegalArgumentException::class.java)

        // null string must throw
        Assertions.assertThatThrownBy { BotCommandUtils.normalizeUserName(null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testGetClickableUserName() {
        // add @
        Assertions.assertThat(
            BotCommandUtils.getClickableUserName("dmitry_weirdo")
        )
            .isEqualTo("@dmitry_weirdo")

        // already has @ -> nothing to add
        Assertions.assertThat(
            BotCommandUtils.getClickableUserName("@dmitry_weirdo")
        )
            .isEqualTo("@dmitry_weirdo")

        // blank string must throw
        Assertions.assertThatThrownBy { BotCommandUtils.getClickableUserName("   \t  ") }
            .isInstanceOf(IllegalArgumentException::class.java)

        // null string must throw
        Assertions.assertThatThrownBy { BotCommandUtils.getClickableUserName(null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testGetBotAdmins() {
        val config = WikiBotConfig(
            botAdmins = listOf(
                "@tonygerm",
                "@Anna543210",
                "DmRodionov",
                "dmitry_weirdo",
                "anutikin",
            )
        )

        val botAdmins = BotCommandUtils.getBotAdmins(config)

        // ordered alphabetically ignore case, cut "@"
        Assertions
            .assertThat(botAdmins)
            .containsExactly(
                "Anna543210",
                "anutikin",
                "dmitry_weirdo",
                "DmRodionov",
                "tonygerm"
            )
    }

    @Test
    fun testFillCommandsWithOverrides() {
        val config = WikiBotConfig(
            commands = mapOf( // override a couple of command names
                "ReloadFromGoogleSheet" to "/reloadFromGoogleSheet",
                "ListAdmins" to "/getAdmins",
            )
        )

        val allCommands = BotCommand.getAllCommands()

        val filledCommands = BotCommandUtils.fillCommands(config)

        Assertions
            .assertThat(filledCommands)
            .hasSize(allCommands.size)

        // overridden name 1
        assertOverriddenCommandName(filledCommands, ReloadFromGoogleSheet::class.java, "/reloadFromGoogleSheet")

        // overridden name 2
        assertOverriddenCommandName(filledCommands, ListAdmins::class.java, "/getAdmins")

        // non-overridden commands must have default command names
        val overriddenClasses: List<Class<*>> = listOf(ReloadFromGoogleSheet::class.java, ListAdmins::class.java)

        filledCommands
            .filter { it.javaClass !in overriddenClasses }
            .forEach { Assertions.assertThat(it.commandText).isEqualTo(it.defaultCommandName) }
    }

    @Test
    fun testFillCommandsWithoutOverrides() {
        val config = WikiBotConfig() // no overrides

        val allCommands = BotCommand.getAllCommands()

        val filledCommands = BotCommandUtils.fillCommands(config)

        Assertions
            .assertThat(filledCommands)
            .hasSize(allCommands.size)

        // all commands must have default command names
        filledCommands
            .forEach { Assertions.assertThat(it.commandText).isEqualTo(it.defaultCommandName) }
    }

    private fun <T : BotCommand> assertOverriddenCommandName(
        filledCommands: List<BotCommand>,
        commandClass: Class<T>,
        expectedOverriddenCommandName: String
    ) {
        val command = filledCommands.filter { it.javaClass == commandClass }

        Assertions.assertThat(command).hasSize(1)
        Assertions.assertThat(command[0].commandText).isEqualTo(expectedOverriddenCommandName)
    }
}
