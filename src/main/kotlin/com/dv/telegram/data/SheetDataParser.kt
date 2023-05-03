package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.google.SheetData
import com.dv.telegram.google.WikiBotGoogleSheet

interface SheetDataParser<T : BotAnswerData> {
    fun getSheetData(sheet: WikiBotGoogleSheet): SheetData

    fun parse(rows: List<RowData>): List<T>

    fun parse(sheet: WikiBotGoogleSheet): List<T> {
        val sheetData = getSheetData(sheet)

        val rows = sheetData.getRowsWithoutFirstRow()

        return parse(rows)
    }

    fun parse(sheetData: SheetData): List<T> {
        val rows = sheetData.getRowsWithoutFirstRow()

        return parse(rows)
    }
}
