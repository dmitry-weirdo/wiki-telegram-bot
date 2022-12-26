package com.dv.telegram.excel

import org.apache.logging.log4j.kotlin.Logging
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime

object PageTreeXlsxWriter : Logging {

    private const val DATE_TIME_FORMAT = "dd.mm.yyyy hh:mm:ss"

    @JvmStatic
    fun main(args: Array<String>) {
        createXlsxFile("c:/java/wiki-telegram-bot/generated-666.xlsx")
    }

    fun createXlsxFile(filePath: String)    {
        val workbook = XSSFWorkbook()

        val sheet = workbook.createSheet("My test sheet name")

        // create styles
        val headerStyle = createHeaderStyle(workbook)
        val linkStyle = createLinkStyle(workbook)
        val dateStyle = createDateTimeStyle(workbook)

        // add header row
        var rowNum = 0
        val headerRow = sheet.createRow(rowNum++)

        val columnNames = listOf(
            "Page",
//            "Page link",
            "Edit date/time",
            "Parent page",
//            "Parent page link"
        )

        var colNum = 0

        columnNames.forEach {
            sheet.setColumnWidth(colNum, 10000)

            val cell = headerRow.createCell(colNum++)
            cell.cellStyle = headerStyle

            cell.setCellValue(it)
        }

        // rows
        val pageRow = sheet.createRow(rowNum++)

        // link cell
        val pageLink = workbook.creationHelper.createHyperlink(HyperlinkType.URL)
        pageLink.address = "https://google.com"

        val pageLinkCell = pageRow.createCell(0)
        pageLinkCell.hyperlink = pageLink
        pageLinkCell.cellStyle = linkStyle
        pageLinkCell.setCellValue("A link to google")

        // last edit date/time cell
        val date = ZonedDateTime.now()

        val dateCell = pageRow.createCell(1)
        dateCell.cellStyle = dateStyle
        dateCell.setCellValue(date.toLocalDateTime())

        // link to parent page cell
        val parentPageLink = workbook.creationHelper.createHyperlink(HyperlinkType.URL)
        parentPageLink.address = "https://yahoo.com"

        val parentPageLinkCell = pageRow.createCell(2)
        parentPageLinkCell.hyperlink = parentPageLink
        parentPageLinkCell.cellStyle = linkStyle
        parentPageLinkCell.setCellValue("A link to Yahoo")

        // write workbook to file
        writeToFile(filePath, workbook)
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val font = workbook.createFont()
        font.bold = true

        val headerStyle = workbook.createCellStyle()
        headerStyle.setFont(font)

        return headerStyle
    }

    private fun createLinkStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val linkFont = workbook.createFont()
        linkFont.underline = XSSFFont.U_SINGLE
        linkFont.color = IndexedColors.BLUE.index

        val linkStyle = workbook.createCellStyle()
        linkStyle.setFont(linkFont)

        return linkStyle
    }

    private fun createDateTimeStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val dateFormat = workbook.creationHelper.createDataFormat().getFormat(DATE_TIME_FORMAT)

        val dateStyle = workbook.createCellStyle()
        dateStyle.dataFormat = dateFormat
        return dateStyle
    }

    private fun writeToFile(filePath: String, workbook: XSSFWorkbook) {
        FileOutputStream(File(filePath)).use {
            workbook.write(it)

            logger.info("Workbook successfully written to file \"$filePath\".")
        }
    }
}
