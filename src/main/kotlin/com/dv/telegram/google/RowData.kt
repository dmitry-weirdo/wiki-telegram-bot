package com.dv.telegram.google

class RowData {
    val cells: MutableList<String> = mutableListOf()

    fun addCell(cell: String) = cells.add(cell)

    fun getCellOrBlank(index: Int): String = cells.getOrElse(index) { "" }
}
