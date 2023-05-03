package com.dv.telegram

import com.dv.telegram.data.BotAnswerData
import com.dv.telegram.data.CityChatsParser
import com.dv.telegram.data.CountryChatsParser
import com.dv.telegram.data.WikiBotCommandsParser
import com.dv.telegram.data.WikiPagesParser
import com.dv.telegram.exception.CommandException
import com.dv.telegram.google.GoogleSheetReader
import com.dv.telegram.google.TabSheetData
import com.dv.telegram.tabs.TabData
import com.dv.telegram.tabs.TabFormat
import org.apache.logging.log4j.kotlin.Logging

/**
 * Wraps the raw string output from [GoogleSheetReader]
 * and parses it to the containers of [BotAnswerData] implementations.
 */
object GoogleSheetLoader : Logging {

    @JvmStatic
    fun readGoogleSheet(config: WikiBotConfig): GoogleSheetBotData {
        try {
            logger.info("Loading bot data from the Google Sheet...")
            val botData = reloadGoogleSheetUnsafe(config)
            logger.info("Bot data successfully reloaded from the Google Sheet.")

            return botData
        }
        catch (e: Exception) {
            logger.error("Error when loading bot data from the Google Sheet", e)
            throw CommandException("При загрузке данных из Google Sheet произошла ошибка.")
        }
    }

    @JvmStatic
    fun readGoogleSheetTabs(config: WikiBotConfig): GoogleSheetBotTabsData {
        try {
            logger.info("Loading bot tabs data from the Google Sheet...")
            val botData = reloadGoogleSheetTabsUnsafe(config)
            logger.info("Bot data successfully reloaded from the Google Sheet.")

            return botData
        }
        catch (e: Exception) {
            logger.error("Error when loading bot tabs data from the Google Sheet", e)
            throw CommandException("При загрузке данных из Google Sheet произошла ошибка.")
        }
    }

    private fun reloadGoogleSheetUnsafe(config: WikiBotConfig): GoogleSheetBotData {
        val wikiBotGoogleSheet = GoogleSheetReader.readGoogleSheetSafe(config)

        val wikiPages = WikiPagesParser().parse(wikiBotGoogleSheet)
        val cityChatsData = CityChatsParser().parse(wikiBotGoogleSheet)
        val countryChatsData = CountryChatsParser().parse(wikiBotGoogleSheet)
        val botCommands = WikiBotCommandsParser().parse(wikiBotGoogleSheet)

        return GoogleSheetBotData(
            wikiPages,
            cityChatsData,
            countryChatsData,
            botCommands,
        )
    }

    private fun reloadGoogleSheetTabsUnsafe(config: WikiBotConfig): GoogleSheetBotTabsData {
        val tabsDataRaw = GoogleSheetReader.readGoogleSheetNew(config)

        val commandTabs = parseSheetData(tabsDataRaw.commandSheets)
        val dataTabs = parseSheetData(tabsDataRaw.dataSheets)

        return GoogleSheetBotTabsData(commandTabs, dataTabs)
    }

    private fun parseSheetData(sheets: List<TabSheetData>) = sheets.map {
        when (it.config.tabFormat) {
            TabFormat.WIKI_PAGES -> TabData(it.config, WikiPagesParser().parse(it.sheet))
            TabFormat.CHATS -> TabData(it.config, CityChatsParser().parse(it.sheet)) // todo: use common ChatsParser instead of CityChatsParser
            TabFormat.COMMANDS -> TabData(it.config, WikiBotCommandsParser().parse(it.sheet))
        }
    }
}
