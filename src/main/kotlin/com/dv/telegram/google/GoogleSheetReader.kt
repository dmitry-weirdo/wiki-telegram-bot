package com.dv.telegram.google

import com.dv.telegram.WikiBotConfig
import com.dv.telegram.util.WikiBotUtils
import com.google.api.client.http.HttpExecuteInterceptor
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException

object GoogleSheetReader : Logging {
    private const val APPLICATION_NAME = "Google Sheets Application Name"

    @JvmStatic
    fun main(args: Array<String>) {
        val config = WikiBotUtils.readConfig()
        readGoogleSheetSafe(config)
    }

    fun readGoogleSheetSafe(config: WikiBotConfig): WikiBotGoogleSheet {
        try {
            return readGoogleSheet(config)
        }
        catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    fun readGoogleSheet(config: WikiBotConfig): WikiBotGoogleSheet {
        val sheetsService = getSheets(config.googleSheetsApiKey)

        val ranges = listOf(
            wrapSheetName(config.wikiPagesSheetName),
            wrapSheetName(config.cityChatsSheetName),
            wrapSheetName(config.countryChatsSheetName),
            wrapSheetName(config.commandsSheetName)
        )

        val readResult = sheetsService
            .spreadsheets()
            .values()
            .batchGet(config.googleSpreadsheetId)
            .setRanges(ranges)
            .execute()

        val valueRanges = readResult.valueRanges

        val wikiPagesSheet = parseSheetData(valueRanges[0], config.wikiPagesSheetName)
        val cityChatsSheet = parseSheetData(valueRanges[1], config.cityChatsSheetName)
        val countryChatsSheet = parseSheetData(valueRanges[2], config.countryChatsSheetName)
        val commandsSheet = parseSheetData(valueRanges[3], config.commandsSheetName)

        return WikiBotGoogleSheet(
            wikiPagesSheet,
            cityChatsSheet,
            countryChatsSheet,
            commandsSheet
        )
    }

    private fun wrapSheetName(sheetName: String) = "'${sheetName}'"

    private fun parseSheetData(sheet: ValueRange, sheetName: String): SheetData {
        logger.info("Parsing sheet \"$sheetName\"...")

        val sheetData = SheetData()

        val sheetValues = sheet.getValues()
        for (sheetRow in sheetValues) {
            val row = RowData()

            for (cellObject in sheetRow) {
                val cellValue = cellObject as String
                row.addCell(cellValue)
            }

            sheetData.addRow(row)
        }

        logger.info("Sheet \"$sheetName\" parsed. Total rows: ${sheetData.rows.size}.")
        return sheetData
    }

    // API-Key auth code copied from https://stackoverflow.com/a/63229676/8534088
    private fun getSheets(apiKey: String): Sheets {
        val transport = NetHttpTransport.Builder().build()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

        val httpRequestInitializer = { request: HttpRequest ->
            request.interceptor = HttpExecuteInterceptor { intercepted ->
                intercepted.url["key"] = apiKey
            }
        }

        return Sheets.Builder(transport, jsonFactory, httpRequestInitializer)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}
