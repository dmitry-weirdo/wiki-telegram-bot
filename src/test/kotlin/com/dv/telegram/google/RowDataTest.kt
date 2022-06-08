package com.dv.telegram.google

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class RowDataTest {

    @Test
    fun testGetCells() {
        val rowData = RowData()

        val fromEmpty = rowData.getCellOrBlank(0)
        Assertions.assertThat(fromEmpty).isNotNull.isBlank

        rowData.addCell(" zero ")
        rowData.addCell("")
        rowData.addCell("two two")
        Assertions.assertThat(rowData.cells).hasSize(3) // cells will execute getter

        Assertions.assertThat(rowData.getCellOrBlank(-1)).isNotNull.isBlank
        Assertions.assertThat(rowData.getCellOrBlank(3)).isNotNull.isBlank
        Assertions.assertThat(rowData.getCellOrBlank(4)).isNotNull.isBlank

        Assertions.assertThat(rowData.getCellOrBlank(0)).isEqualTo(" zero ")
        Assertions.assertThat(rowData.getCellOrBlank(1)).isNotNull.isBlank
        Assertions.assertThat(rowData.getCellOrBlank(2)).isEqualTo("two two")
    }
}
