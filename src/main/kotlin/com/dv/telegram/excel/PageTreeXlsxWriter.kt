package com.dv.telegram.excel

import com.dv.telegram.notion.NotionPageNode
import org.apache.logging.log4j.kotlin.Logging
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object PageTreeXlsxWriter : Logging {

    private const val DATE_TIME_FORMAT = "dd.mm.yyyy hh:mm:ss"

    private val FILE_NAME_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy.MM.dd HH-mm-ss")

    private const val SHEET_NAME = "uahelp.wiki pages tree"

    private const val HEADER_ROW_NUM = 0
    private const val FIRST_DATA_ROW_NUM = 1

    private val COLUMN_NAMES = listOf(
        // listOf cannot be marked as const
        "Page",
//            "Page link",
        "Edit date/time",
        "Parent page",
//            "Parent page link"
    )

    private val COLUMN_WIDTHS = listOf(
        20000,
        5000,
        15000,
    )

    @JvmStatic
    fun main(args: Array<String>) {
        createXlsxFile(
            "c:/java/wiki-telegram-bot/generated-666.xlsx",
            ::appendTestDataRows
        )
    }

    fun getFilePath(directory: String, rootPageId: String, generationDate: ZonedDateTime): String {
        val dateTimeString = generationDate.format(FILE_NAME_DATE_PATTERN)

        val fileName = "pages-tree__root-${rootPageId}__$dateTimeString.xlsx"

        return "$directory${File.separator}$fileName"
    }

    fun createXlsxFile(filePath: String, nodes: List<NotionPageNode>) {
        val rowsAppender = getAppendPageNodes(nodes)

        createXlsxFile(filePath, rowsAppender)
    }

    fun createXlsxFile(
        filePath: String,
        rowsAppender: (RowGenerationContext) -> Unit
    ) {
        val workbook = XSSFWorkbook()

        val sheet = workbook.createSheet(SHEET_NAME)

        // create styles
        val headerStyle = createHeaderStyle(workbook)
        val linkStyle = createLinkStyle(workbook)
        val dateStyle = createDateTimeStyle(workbook)

        // add header row
        val headerRow = sheet.createRow(HEADER_ROW_NUM)

        var colNum = 0

        COLUMN_NAMES.forEach {
            sheet.setColumnWidth(colNum, COLUMN_WIDTHS[colNum])

            val cell = headerRow.createCell(colNum++)
            cell.cellStyle = headerStyle

            cell.setCellValue(it)
        }

        val context = RowGenerationContext(workbook, sheet, linkStyle, dateStyle)

        rowsAppender(context)

        // write workbook to file
        writeToFile(filePath, workbook)
    }

    private fun appendTestDataRows(
        context: RowGenerationContext
    ) {
        // rows
        var rowNum = FIRST_DATA_ROW_NUM
        val pageRow = context.sheet.createRow(rowNum++)

        // link to page cell
        val pageLink = context.workbook.creationHelper.createHyperlink(HyperlinkType.URL)
        pageLink.address = "https://google.com"

        val pageLinkCell = pageRow.createCell(0)
        pageLinkCell.hyperlink = pageLink
        pageLinkCell.cellStyle = context.linkStyle
        pageLinkCell.setCellValue("${NotionPageNode.DEFAULT_SEPARATOR}A link to google 1")

        // last edit date/time cell
        val date = ZonedDateTime.now()

        val dateCell = pageRow.createCell(1)
        dateCell.cellStyle = context.dateStyle
        dateCell.setCellValue(date.toLocalDateTime())

        // link to parent page cell
        val parentPageLink = context.workbook.creationHelper.createHyperlink(HyperlinkType.URL)
        parentPageLink.address = "https://yahoo.com"

        val parentPageLinkCell = pageRow.createCell(2)
        parentPageLinkCell.hyperlink = parentPageLink
        parentPageLinkCell.cellStyle = context.linkStyle
        parentPageLinkCell.setCellValue("A link to Yahoo 1")
    }

    private fun getAppendPageNodes(nodes: List<NotionPageNode>): (RowGenerationContext) -> Unit {
        return { context ->
            var rowNum = FIRST_DATA_ROW_NUM

            nodes.forEach {
                val pageRow = context.sheet.createRow(rowNum)

                // link to page cell
                val pageLink = context.workbook.creationHelper.createHyperlink(HyperlinkType.URL)
                pageLink.address = it.getLink()

                val pageLinkCell = pageRow.createCell(0)
                pageLinkCell.hyperlink = pageLink
                pageLinkCell.cellStyle = context.linkStyle

                val separator = it.getSeparator("    ")
                pageLinkCell.setCellValue("$separator${it.title}")

                // last edit date/time cell
                val dateCell = pageRow.createCell(1)
                dateCell.cellStyle = context.dateStyle
                dateCell.setCellValue(it.lastEditedTime.toLocalDateTime())

                // link to parent page cell
                if (it.parent != null) {
                    val parentPageLink = context.workbook.creationHelper.createHyperlink(HyperlinkType.URL)
                    parentPageLink.address = it.getParentLink()

                    val parentPageLinkCell = pageRow.createCell(2)
                    parentPageLinkCell.hyperlink = parentPageLink
                    parentPageLinkCell.cellStyle = context.linkStyle
                    parentPageLinkCell.setCellValue("${it.parent?.title}")
                }

                // get ready for the next row
                rowNum++
            }
        }
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

data class RowGenerationContext (
    val workbook: XSSFWorkbook,
    val sheet: XSSFSheet,
    val linkStyle: XSSFCellStyle,
    val dateStyle: XSSFCellStyle,
)
