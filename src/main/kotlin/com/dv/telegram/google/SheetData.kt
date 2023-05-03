package com.dv.telegram.google

/**
 * Raw Google Sheet data, all cells are read
 * as [rows][RowData] of strings.
 */
class SheetData {
    val rows: MutableList<RowData> = mutableListOf()

    fun addRow(row: RowData) = rows.add(row)

    fun getRowsWithoutFirstRow() = rows.subList(1, rows.size)
}
