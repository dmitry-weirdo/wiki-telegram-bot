package com.dv.telegram.data;

import com.dv.telegram.WikiPageData;
import com.dv.telegram.google.RowData;
import com.dv.telegram.google.SheetData;
import com.dv.telegram.google.WikiBotGoogleSheet;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class WikiPagesParser {

    public static List<WikiPageData> parseWikiPages(WikiBotGoogleSheet sheet) {
        SheetData wikiPagesSheet = sheet.getWikiPagesSheet();
        List<RowData> rows = wikiPagesSheet.getRowsWithoutFirstRow();

        List<WikiPageData> pages = new ArrayList<>();

        int rowNum = 1;

        for (RowData row : rows) {
            String pageName = row.getCellOrBlank(0);
            String pageUrl = row.getCellOrBlank(1);
            String wordsString = row.getCellOrBlank(2);

            List<String> words = DataUtils.parseWords(wordsString);

            WikiPageData pageData = new WikiPageData(pageName, pageUrl, wordsString, words);
            pages.add(pageData);

            log.info("Row {}: {} / {} / {}", rowNum, pageName, pageUrl, words);
            rowNum++;
        }

        return pages;
    }
}
