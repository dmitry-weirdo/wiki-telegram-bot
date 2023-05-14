package com.dv.telegram.google

import com.dv.telegram.WikiBotConfig
import com.dv.telegram.exception.WikiBotException
import com.dv.telegram.tabs.TabConfigs
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
        readGoogleSheetSafe(config, config.tabs)
    }

    fun readGoogleSheetSafe(config: WikiBotConfig, tabConfigs: TabConfigs): WikiBotGoogleSheetTabsData {
        try {
            return readGoogleSheet(config, tabConfigs)
        }
        catch (e: IOException) {
            throw WikiBotException(e)
        }
    }

    fun readGoogleSheet(config: WikiBotConfig, tabConfigs: TabConfigs): WikiBotGoogleSheetTabsData {
        val allTabNames = getAllTabNames(tabConfigs)

        val sheetsService = getSheets(config.googleSheetsApiKey)

        // read all tabs in one iteration
        val ranges = createRanges(tabConfigs)

        val readResult = sheetsService
            .spreadsheets()
            .values()
            .batchGet(config.googleSpreadsheetId)
            .setRanges(ranges)
            .execute()

        val valueRanges = readResult.valueRanges

        val allSheetsData = allTabNames.mapIndexed {
            index, value -> parseSheetData(valueRanges[index], value)
        }

        // map allSheetsData and tab configs to WikiBotGoogleSheetTabsData
        var sheetIndex = 0

        val commandSheets = tabConfigs
            .commandTabs
            .map { TabSheetData(it, allSheetsData[sheetIndex++]) }

        val dataSheets = tabConfigs
            .dataTabs
            .map { TabSheetData(it, allSheetsData[sheetIndex++]) }

        return WikiBotGoogleSheetTabsData(commandSheets, dataSheets)
    }

    private fun getAllTabNames(configs: TabConfigs): List<String> {
        val commandTabNames = configs.commandTabs.map { it.tabName }
        val dataTabNames = configs.dataTabs.map { it.tabName }

        return commandTabNames + dataTabNames
    }

    private fun createRanges(configs: TabConfigs): List<String> {
        val commandTabsRange = configs.commandTabs.map { wrapSheetName(it.tabName) }

        val dataTabsRange = configs.dataTabs.map { wrapSheetName(it.tabName) }

        return commandTabsRange + dataTabsRange
    }

    private fun wrapSheetName(sheetName: String) = "'$sheetName'"

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
