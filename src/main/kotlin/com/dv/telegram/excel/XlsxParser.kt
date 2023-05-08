package com.dv.telegram.excel

import com.dv.telegram.data.DataUtils.parseWords
import com.dv.telegram.data.WikiPageData
import com.dv.telegram.exception.WikiBotException
import org.apache.logging.log4j.kotlin.Logging
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.IOException

object XlsxParser : Logging { // todo: this class has to read to WikiBotGoogleSheet (i.e. SheetData), like GoogleSheetReader

    private const val FILE_NAME = "Wiki-pages-and-keywords.xlsx"

    @JvmStatic
    fun main(args: Array<String>) {
        parseWikiPagesDataSafe()
    }

    fun parseWikiPagesDataSafe(): List<WikiPageData> {
        return try {
            parseWikiPagesData()
        }
        catch (e: IOException) {
            throw WikiBotException(e)
        }
    }

    @Throws(IOException::class)
    fun parseWikiPagesData(): List<WikiPageData> {
        val workbook = parseWorkbook(FILE_NAME)

        val sheet = workbook.getSheetAt(0)

        val pages: MutableList<WikiPageData> = ArrayList()

        val firsRowNum = sheet.firstRowNum + 1 // skip header row
        val lastRowNum = sheet.lastRowNum

        for (rowNum in firsRowNum..lastRowNum) {
            val row = sheet.getRow(rowNum)

            val pageName = getStringSafe(row, 0)
            val pageUrl = getStringSafe(row, 1)
            val wordsString = getStringSafe(row, 2).lowercase() // assure lowercase words

            val words = parseWords(wordsString)

            val pageData = WikiPageData(pageName, pageUrl, wordsString, words)
            pages.add(pageData)

            logger.info("Row $rowNum: $pageName / $pageUrl / $words")
        }

        logger.info("Total pages collected: ${pages.size}")
        return pages
    }

    @Throws(IOException::class)
    private fun parseWorkbook(fileName: String): Workbook {
        javaClass.classLoader.getResourceAsStream(fileName).use { stream -> // getResource does not work within jar!
            return XSSFWorkbook(stream)
        }
    }

    private fun getStringSafe(row: Row, cellNumber: Int): String {
        val cell = row.getCell(cellNumber)
            ?: return ""

        return cell.stringCellValue
    }
}
