package com.dv.telegram

import com.dv.telegram.data.ChatData
import com.dv.telegram.data.WikiBotCommandData
import com.dv.telegram.data.WikiPageData
import com.dv.telegram.tabs.TabConfig
import com.dv.telegram.tabs.TabData
import com.dv.telegram.tabs.TabFormat
import com.dv.telegram.tabs.TabType
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

        val botTabsData = getBotTabsData()

        val context = WikiBotsContext()

        val bot = WikiBot(
            context,
            updatedConfig,
            botTabsData
        )

        bot.telegramName = "test_telegram_wiki_bot"

        return bot
    }

    @Suppress("LongMethod")
    private fun getBotTabsData(): GoogleSheetBotTabsData {
        val commandsTabData = TabData(
            TabConfig(
                tabName = "Команды бота",
                tabFormat = TabFormat.COMMANDS,
                tabType = TabType.COMMANDS,
                showHeader = false,
                header = "Ответы бота",
                bullet = "—"
            ),
            listOf(
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
        )

        val wikiPageTabData = TabData(
            TabConfig(
                tabName = "Страницы вики",
                tabFormat = TabFormat.WIKI_PAGES,
                tabType = TabType.WIKI_PAGES,
                showHeader = false,
                header = "\uD83D\uDCC1 Страницы вики"
            ),
            listOf(
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
                )
            )
        )

        val cityChatsTabConfig = TabConfig(
            tabName = "Чаты городов",
            tabFormat = TabFormat.CHATS,
            tabType = TabType.CITY_CHATS,
            showHeader = false,
            header = "\uD83C\uDDE9\uD83C\uDDEA Чаты по городам Германии",
            bullet = "—"
        )

        val cityChatsTabData = TabData(
            cityChatsTabConfig,
            listOf(
                ChatData(
                    "Augsburg",
                    "Augsburg, Аугсбург, Аугбург, Augburg, аугзбург",
                    listOf("аугсбург", "аугсбург", "аугбург", "augburg", "аугзбург"),
                    listOf(
                        "https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине",
                        "https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины",
                        "https://t.me/Ukr_Augsburg_help — Українці Augsburg"
                    ),
                    cityChatsTabConfig
                ),
                ChatData(
                    "Eisenach",
                    "Eisenach, Айзенах",
                    listOf("eisenach", "айзенах"),
                    listOf(
                        "https://t.me/HelpUkraine_Eisenach — Help Ukraine \\uD83C\\uDDFA\\uD83C\\uDDE6 in Eisenach \\uD83C\\uDDE9\\uD83C\\uDDEA"
                    ),
                    cityChatsTabConfig
                )
            )
        )

        val countryChatsTabConfig = TabConfig(
            tabName = "Чаты стран",
            tabFormat = TabFormat.CHATS,
            tabType = TabType.COUNTRY_CHATS,
            showHeader = false,
            header = "\uD83C\uDF0E Чаты по странам",
            bullet = "—"
        )

        val countryChatsTabData = TabData(
            countryChatsTabConfig,
            listOf(
                ChatData(
                    "Болгария",
                    "Болгария, Болгарія, Bulgaria, Bulgarien, Bolgaria, Bulgaria",
                    listOf("болгария", "болгарія", "bulgaria", "bulgarien", "bolgaria", "bulgaria"),
                    listOf(
                        "https://t.me/UAhelpinfo/28 — Общая информация"
                    ),
                    countryChatsTabConfig
                ),
                ChatData(
                    "Португалия",
                    "Португалия, Португалія, Portugal, Portugalia",
                    listOf("португалия", "португалія", "portugal", "portugalia"),
                    listOf(
                        "https://t.me/UAhelpinfo/89 — Общая информация",
                        "https://t.me/toportugal — Из Украины в Португалию",
                        "https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи"
                    ),
                    countryChatsTabConfig
                )
            )
        )

        val commandTabs = listOf(
            commandsTabData
        )

        val dataTabs = listOf(
            wikiPageTabData,
            cityChatsTabData,
            countryChatsTabData
        )

        return GoogleSheetBotTabsData(commandTabs, dataTabs)
    }

    @JvmStatic
    fun getUpdate(): Update {
        val chat = Chat()
        chat.id = 123456789
        chat.type = "group" // not "private", bot name required
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
