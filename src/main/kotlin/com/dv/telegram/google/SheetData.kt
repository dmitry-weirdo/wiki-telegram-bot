package com.dv.telegram.google

class SheetData {
    val rows: MutableList<RowData> = mutableListOf()

    fun addRow(row: RowData) = rows.add(row)

    fun getRowsWithoutFirstRow() = rows.subList(1, rows.size)
}
