package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.google.SheetData
import com.dv.telegram.google.WikiBotGoogleSheet
import com.dv.telegram.tabs.TabFormat

/**
 * Parses the raw strings from [SheetData]
 * to a format/data-specific [List]
 * of [BotAnswerData] implementations.
 */
interface SheetDataParser<T : BotAnswerData> {

    /**
     * Which tab from the [WikiBotGoogleSheet] to parse.
     */
    fun getSheetData(sheet: WikiBotGoogleSheet): SheetData

    /**
     * Which tab format does the implementation parse.
     */
    fun getTabFormat(): TabFormat

    /**
     * Parses the given list of raw strings of row data
     * to a list of format-specific [BotAnswerData].
     */
    fun parse(rows: List<RowData>): List<T>

    /**
     * Parses the sheet defined by [getSheetData].
     */
    fun parse(sheet: WikiBotGoogleSheet): List<T> {
        val sheetData = getSheetData(sheet)

        val rows = sheetData.getRowsWithoutFirstRow()

        return parse(rows)
    }

    /**
     * Parses any given sheet.
     */
    fun parse(sheetData: SheetData): List<T> {
        val rows = sheetData.getRowsWithoutFirstRow()

        return parse(rows)
    }
}
