package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.google.SheetData
import com.dv.telegram.tabs.TabConfig
import com.dv.telegram.tabs.TabFormat

/**
 * Parses the raw strings from [SheetData]
 * to a format/data-specific [List]
 * of [BotAnswerData] implementations.
 */
interface SheetDataParser<T : BotAnswerData> {

    /**
     * Which tab format does the implementation parse.
     */
    fun getTabFormat(): TabFormat

    /**
     * Parses the given list of raw strings of row data
     * to a list of format-specific [BotAnswerData].
     */
    fun parse(rows: List<RowData>, tabConfig: TabConfig): List<T>

    /**
     * Parses any given sheet.
     */
    fun parse(sheetData: SheetData, tabConfig: TabConfig): List<T> {
        val rows = sheetData.getRowsWithoutFirstRow()

        return parse(rows, tabConfig)
    }
}
