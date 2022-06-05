package com.dv.telegram.data;

import com.dv.telegram.google.RowData;
import com.dv.telegram.google.SheetData;
import com.dv.telegram.google.WikiBotGoogleSheet;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class WikiPagesParser implements SheetDataParser<WikiPageData> {

    @Override
    public SheetData getSheetData(WikiBotGoogleSheet sheet) {
        return sheet.getWikiPagesSheet();
    }

    @Override
    public List<WikiPageData> parse(List<RowData> rows) {
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

        log.info("Total {} wiki pages parsed.", pages.size());

        return pages;
    }
}
