package com.dv.telegram.google;

import com.dv.telegram.WikiBotConfig;
import com.dv.telegram.util.WikiBotUtils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.List;

@Log4j2
public class GoogleSheetReader {

    private static final String APPLICATION_NAME = "Google Sheets Application Name";

    public static void main(String[] args) {
        WikiBotConfig config = WikiBotUtils.readConfig();
        readGoogleSheetSafe(config);
    }

    public static WikiBotGoogleSheet readGoogleSheetSafe(WikiBotConfig config) {
        try {
            return readGoogleSheet(config);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static WikiBotGoogleSheet readGoogleSheet(WikiBotConfig config) throws IOException {
        Sheets sheetsService = getSheets(config.googleSheetsApiKey);

        List<String> ranges = List.of(
            wrapSheetName(config.wikiPagesSheetName),
            wrapSheetName(config.cityChatsSheetName),
            wrapSheetName(config.countryChatsSheetName),
            wrapSheetName(config.commandsSheetName)
        );

        BatchGetValuesResponse readResult = sheetsService
            .spreadsheets()
            .values()
            .batchGet(config.googleSpreadsheetId)
            .setRanges(ranges)
            .execute();

        List<ValueRange> valueRanges = readResult.getValueRanges();

        SheetData wikiPagesSheet = parseSheetData(valueRanges.get(0), config.wikiPagesSheetName);
        SheetData cityChatsSheet = parseSheetData(valueRanges.get(1), config.cityChatsSheetName);
        SheetData countryChatsSheet = parseSheetData(valueRanges.get(2), config.countryChatsSheetName);
        SheetData commandsSheet = parseSheetData(valueRanges.get(3), config.commandsSheetName);

        return new WikiBotGoogleSheet(
            wikiPagesSheet,
            cityChatsSheet,
            countryChatsSheet,
            commandsSheet
        );
    }

    private static String wrapSheetName(String sheetName) {
        return String.format("'%s'", sheetName);
    }

    private static SheetData parseSheetData(ValueRange sheet1, String sheetName) {
        log.info("Parsing sheet {}...", sheetName);

        SheetData sheetData = new SheetData();

        List<List<Object>> sheetValues = sheet1.getValues();
        for (List<Object> sheetRow : sheetValues) {
            RowData row = new RowData();

            for (Object cellObject : sheetRow) {
                String cellValue = (String) cellObject;
                row.addCell(cellValue);
            }

            sheetData.addRow(row);
        }

        log.info("Sheet {} parsed. Total rows: {}.", sheetName, sheetData.getRows().size());
        return sheetData;
    }

    // API-Key auth code copied from https://stackoverflow.com/a/63229676/8534088
    private static Sheets getSheets(String apiKey) {
        NetHttpTransport transport = new NetHttpTransport.Builder().build();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        HttpRequestInitializer httpRequestInitializer = request ->
            request.setInterceptor(intercepted ->
                intercepted
                    .getUrl()
                    .set("key", apiKey)
            );

        return new Sheets.Builder(transport, jsonFactory, httpRequestInitializer)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
}
