package com.dv.telegram.data

import com.dv.telegram.google.RowData
import com.dv.telegram.tabs.TabConfig
import com.dv.telegram.tabs.TabFormat
import org.apache.logging.log4j.kotlin.Logging

class WikiPagesParser : SheetDataParser<WikiPageData>, Logging {

    override fun getTabFormat(): TabFormat {
        return TabFormat.WIKI_PAGES
    }

    override fun parse(rows: List<RowData>, tabConfig: TabConfig): List<WikiPageData> {
        val pages = mutableListOf<WikiPageData>()

        var rowNum = 1

        for (row in rows) {
            val pageName = row.getCellOrBlank(0)
            val pageUrl = row.getCellOrBlank(1)
            val wordsString = row.getCellOrBlank(2)
            val words = DataUtils.parseWords(wordsString)

            val pageData = WikiPageData(pageName, pageUrl, wordsString, words)
            pages.add(pageData)

            logger.info("Row $rowNum: $pageName / $pageUrl / $words")
            rowNum++
        }

        logger.info("Total ${pages.size} wiki pages parsed.")

        return pages
    }
}
