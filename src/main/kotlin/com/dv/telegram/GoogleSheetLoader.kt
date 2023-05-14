package com.dv.telegram

import com.dv.telegram.data.BotAnswerData
import com.dv.telegram.data.ChatsParser
import com.dv.telegram.data.WikiBotCommandsParser
import com.dv.telegram.data.WikiPagesParser
import com.dv.telegram.exception.CommandException
import com.dv.telegram.google.GoogleSheetReader
import com.dv.telegram.google.TabSheetData
import com.dv.telegram.tabs.TabConfigs
import com.dv.telegram.tabs.TabData
import com.dv.telegram.tabs.TabFormat
import org.apache.logging.log4j.kotlin.Logging

/**
 * Wraps the raw string output from [GoogleSheetReader]
 * and parses it to the containers of [BotAnswerData] implementations.
 */
object GoogleSheetLoader : Logging {

    @JvmStatic
    fun readGoogleSheetTabs(config: WikiBotConfig) = readGoogleSheetTabs(config, config.tabs)

    @JvmStatic
    fun readGoogleSheetTabs(config: WikiBotConfig, tabConfigs: TabConfigs): GoogleSheetBotTabsData {
        try {
            logger.info("Loading bot tabs data from the Google Sheet...")
            val botData = reloadGoogleSheetTabsUnsafe(config, tabConfigs)
            logger.info("Bot data successfully reloaded from the Google Sheet.")

            return botData
        }
        catch (e: Exception) {
            logger.error("Error when loading bot tabs data from the Google Sheet", e)
            throw CommandException("При загрузке данных из Google Sheet произошла ошибка.")
        }
    }

    private fun reloadGoogleSheetTabsUnsafe(config: WikiBotConfig, tabConfigs: TabConfigs): GoogleSheetBotTabsData {
        val tabsDataRaw = GoogleSheetReader.readGoogleSheet(config, tabConfigs)

        val commandTabs = parseSheetData(tabsDataRaw.commandSheets)
        val dataTabs = parseSheetData(tabsDataRaw.dataSheets)

        return GoogleSheetBotTabsData(commandTabs, dataTabs)
    }

    private fun parseSheetData(sheets: List<TabSheetData>) = sheets.map {
        val parser = when(it.config.tabFormat) {
            TabFormat.WIKI_PAGES -> WikiPagesParser()
            TabFormat.CHATS -> ChatsParser()
            TabFormat.COMMANDS -> WikiBotCommandsParser()
        }

        val answers = parser.parse(it.sheet, it.config)

        TabData(it.config, answers)
    }
}
