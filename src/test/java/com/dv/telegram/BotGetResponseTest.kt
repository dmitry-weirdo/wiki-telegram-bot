package com.dv.telegram

import com.dv.telegram.command.GetEnvironment
import com.dv.telegram.command.GetStatistics
import com.dv.telegram.util.JacksonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

internal class BotGetResponseTest {

    @Test
    @DisplayName("Message without bot name must not trigger the bot.")
    fun testMessageWithoutBotNameIsNotForTheBot() {
        val wikiBot = getWikiBot();

        val result = wikiBot.getResponseText("message not for the bot", "userName")

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot());
    }

    @Test
    @DisplayName("Message without text must not trigger the bot.")
    fun testEmptyMessageIsNotForTheBot() {
        val wikiBot = getWikiBot();

        val result = wikiBot.getResponseText("", "userName")

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot());
    }

    @Test
    @DisplayName("Special command executed by bot admin must return the special command response.")
    fun testGetSpecialCommandResponseCalledByAdmin() {
        val wikiBot = getWikiBot();

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next();

        val getEnvironment = GetEnvironment()

        val result = wikiBot.getResponseText("$botName ${getEnvironment.defaultCommandName}", botAdmin)

        val expectedResult = MessageProcessingResult.specialCommand(
            Optional.of(getEnvironment.getResponse("", wikiBot)),
            false
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by NOT bot admin must return the non-special command response.")
    fun testGetSpecialCommandResponseCalledByNotAdminWithCommandOverride() {
        val wikiBot = getWikiBot();

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next();
        val notBotAdmin = botAdmin + "_"

        val getEnvironment = GetEnvironment()

        val result = wikiBot.getResponseText("$botName ${getEnvironment.defaultCommandName}", notBotAdmin)

        val expectedResult = MessageProcessingResult.answerFound("Override of the special command!")

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by NOT bot admin must return \"result not found\" response.")
    fun testGetSpecialCommandResponseCalledByNotAdminWithNoOverride() {
        val wikiBot = getWikiBot();

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next();
        val notBotAdmin = botAdmin + "_"

        val getStatistics = GetStatistics()

        val text = "$botName ${getStatistics.defaultCommandName}"
        val result = wikiBot.getResponseText(text, notBotAdmin)

        val expectedResult = MessageProcessingResult.answerNotFound(
            Optional.of(wikiBot.getNoResultAnswer(text))
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    private fun getWikiBot(): WikiBot {
        val configFilePath = BotGetResponseTest::class.java.getResource("/test-config.json").path
        val configs = JacksonUtils.parseConfigs(configFilePath)
        val config = configs.configs[0]

        val botData = getBotData()

        return WikiBot(
            config,
            botData
        )
    }

    private fun getBotData(): GoogleSheetBotData {
        val pages = listOf(
            WikiPageData(
                "Стартовая страница вики",
                "https://uahelp.wiki",
                "вики, wiki, вікі",
                listOf("вики", "wiki", "вікі")
            ),
            WikiPageData(
                "Правила безопасности",
                "https://uahelp.wiki/safety",
                "безопасност, безпек",
                listOf("безопасност", "безпек")
            ),
        )

        val cityChats = listOf(
            CityChatData(
                "Augsburg",
                "Аугсбург, Аугбург, Augburg, аугзбург",
                listOf("Аугсбург", "Аугбург", "Augburg", "аугзбург"),
                listOf(
                    "https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине",
                    "https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины",
                    "https://t.me/Ukr_Augsburg_help — Українці Augsburg"
                )
            ),
            CityChatData(
                "Eisenach",
                "Eisenach, Айзенах",
                listOf("Eisenach", "Айзенах"),
                listOf(
                    "https://t.me/HelpUkraine_Eisenach — Help Ukraine \\uD83C\\uDDFA\\uD83C\\uDDE6 in Eisenach \\uD83C\\uDDE9\\uD83C\\uDDEA"
                )
            )
        )

        val countryChats = listOf(
            CountryChatData(
                "Болгария",
                "Болгария, Болгарія, Bulgaria, Bulgarien, Bolgaria, Bulgaria",
                listOf("Болгария", "Болгарія", "Bulgaria", "Bulgarien", "Bolgaria", "Bulgaria"),
                listOf(
                    "https://t.me/UAhelpinfo/28 — Общая информация"
                )
            ),
            CountryChatData(
                "Португалия",
                "Португалия, Португалія, Portugal, Portugalia",
                listOf("Португалия", "Португалія", "Portugal", "Portugalia"),
                listOf(
                    "https://t.me/UAhelpinfo/89 — Общая информация",
                    "https://t.me/toportugal — Из Украины в Португалию",
                    "https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи"
                )
            )
        )

        val commands = listOf(
            WikiBotCommandData(
                "Добрый",
                "Добрый день, добрый вечер",
                listOf("Добрый день", "добрый вечер")
            ),
            WikiBotCommandData(
                "Героям слава!",
                "Слава Украине, Слава Україні, Слава Украини",
                listOf("Слава Украине", "Слава Україні", "Слава Украини")
            ),
            WikiBotCommandData(
                "Override of the special command!",
                "/getEnvironment",
                listOf("/getenvironment")
            )
        )

        return GoogleSheetBotData(
            pages,
            cityChats,
            countryChats,
            commands
        )
    }
}
