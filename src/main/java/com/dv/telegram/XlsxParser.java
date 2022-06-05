package com.dv.telegram;

import com.dv.telegram.data.DataUtils;
import com.dv.telegram.data.WikiPageData;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Log4j2
public class XlsxParser {

    private static final String FILE_NAME = "Wiki-pages-and-keywords.xlsx";

    public static void main(String[] args) throws IOException {
        parseWikiPagesData();
    }

    public static List<WikiPageData> parseWikiPagesDataSafe() {
        try {
            return parseWikiPagesData();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<WikiPageData> parseWikiPagesData() throws IOException {
        Workbook workbook = parseWorkbook(FILE_NAME);

        Sheet sheet = workbook.getSheetAt(0);

        List<WikiPageData> pages = new ArrayList<>();

        int firsRowNum = sheet.getFirstRowNum() + 1; // skip header row
        int lastRowNum = sheet.getLastRowNum();

        for (int rowNum = firsRowNum; rowNum <= lastRowNum; rowNum++) {
            Row row = sheet.getRow(rowNum);

            String pageName = getStringSafe(row,0);
            String pageUrl = getStringSafe(row,1);
            String wordsString = getStringSafe(row,2).toLowerCase(Locale.ROOT); // assure lowercase words

            List<String> words = DataUtils.parseWords(wordsString);

            WikiPageData pageData = new WikiPageData(pageName, pageUrl, wordsString, words);
            pages.add(pageData);

            log.info("Row {}: {} / {} / {}", rowNum, pageName, pageUrl, words);
        }

        log.info("Total pages collected: {}", pages.size());
        return pages;
    }

    private static Workbook parseWorkbook(String fileName) throws IOException {
        ClassLoader classLoader = XlsxParser.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(fileName)) { // getResource does not work within jar!
            return new XSSFWorkbook(stream);
        }
    }

    private static String getStringSafe(Row row, int cellNumber) {
        Cell cell = row.getCell(cellNumber);
        if (cell == null) {
            return "";
        }

        return cell.getStringCellValue();
    }
}
