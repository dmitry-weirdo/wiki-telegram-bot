package com.dv.telegram

import com.dv.telegram.data.CityChatData
import com.dv.telegram.data.CountryChatData
import com.dv.telegram.data.WikiBotCommandData
import com.dv.telegram.data.WikiPageData
import com.dv.telegram.util.JacksonUtils
import org.assertj.core.api.Assertions
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

object BotTestUtils {

    private const val TEST_CONFIG_FILE_PATH = "/test-config.json"

    @JvmStatic
    fun getWikiBot(
        configUpdater: (config: WikiBotConfig) -> WikiBotConfig = { it } // by default, do not change config read from file
    ): WikiBot {
        val configFilePath = BotGetResponseTest::class.java.getResource(TEST_CONFIG_FILE_PATH)?.path
        Assertions.assertThat(configFilePath).isNotNull

        val configs = JacksonUtils.parseConfigs(configFilePath!!)
        val config = configs.configs[0]
        val updatedConfig = configUpdater(config)

        val botData = getBotData()

        val context = WikiBotsContext()

        val bot = WikiBot(
            context,
            updatedConfig,
            botData
        )

        bot.telegramName = "test_telegram_wiki_bot"

        return bot
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
                "Augsburg, Аугсбург, Аугбург, Augburg, аугзбург",
                listOf("аугсбург", "аугсбург", "аугбург", "augburg", "аугзбург"),
                listOf(
                    "https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине",
                    "https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины",
                    "https://t.me/Ukr_Augsburg_help — Українці Augsburg"
                )
            ),
            CityChatData(
                "Eisenach",
                "Eisenach, Айзенах",
                listOf("eisenach", "айзенах"),
                listOf(
                    "https://t.me/HelpUkraine_Eisenach — Help Ukraine \\uD83C\\uDDFA\\uD83C\\uDDE6 in Eisenach \\uD83C\\uDDE9\\uD83C\\uDDEA"
                )
            )
        )

        val countryChats = listOf(
            CountryChatData(
                "Болгария",
                "Болгария, Болгарія, Bulgaria, Bulgarien, Bolgaria, Bulgaria",
                listOf("болгария", "болгарія", "bulgaria", "bulgarien", "bolgaria", "bulgaria"),
                listOf(
                    "https://t.me/UAhelpinfo/28 — Общая информация"
                )
            ),
            CountryChatData(
                "Португалия",
                "Португалия, Португалія, Portugal, Portugalia",
                listOf("португалия", "португалія", "portugal", "portugalia"),
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
                listOf("добрый день", "добрый вечер")
            ),
            WikiBotCommandData(
                "Героям слава!",
                "Слава Украине, Слава Україні, Слава Украини",
                listOf("слава украине", "слава україні", "слава украини")
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

    @JvmStatic
    fun getUpdate(): Update {
        val chat = Chat()
        chat.id = 123456789
        chat.type = "private"
        chat.title = "Test fake chat title"

        val message = Message()
        message.chat = chat
        message.date = 123456
        message.text = "Test fake message text"

        val update = Update()
        update.message = message

        return update
    }
}
