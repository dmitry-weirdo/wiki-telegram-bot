package com.dv.telegram

import com.dv.telegram.util.JacksonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BotGetResponseTest {

    @Test
    fun testMessageWithoutBotNameIsNotForTheBot() {
        val wikiBot = getWikiBot();

        val result = wikiBot.getResponseText("message not for the bot", "userName")

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot());
    }

    @Test
    fun testEmptyMessageIsNotForTheBot() {
        val wikiBot = getWikiBot();

        val result = wikiBot.getResponseText("", "userName")

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot());
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
