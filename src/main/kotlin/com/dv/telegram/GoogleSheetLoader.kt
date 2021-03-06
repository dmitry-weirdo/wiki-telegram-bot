package com.dv.telegram

import com.dv.telegram.data.CityChatsParser
import com.dv.telegram.data.CountryChatsParser
import com.dv.telegram.data.WikiBotCommandsParser
import com.dv.telegram.data.WikiPagesParser
import com.dv.telegram.exception.CommandException
import com.dv.telegram.google.GoogleSheetReader
import org.apache.logging.log4j.kotlin.Logging

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
            botCommands
        )
    }
}
