package com.dv.telegram.data;

import com.dv.telegram.google.RowData;
import com.dv.telegram.google.SheetData;
import com.dv.telegram.google.WikiBotGoogleSheet;

import java.util.List;

public interface SheetDataParser<T> {

    SheetData getSheetData(WikiBotGoogleSheet sheet);

    List<T> parse(List<RowData> rows);

    default List<T> parse(WikiBotGoogleSheet sheet) {
        SheetData sheetData = getSheetData(sheet);

        List<RowData> rows = sheetData.getRowsWithoutFirstRow();

        return parse(rows);
    }
}
