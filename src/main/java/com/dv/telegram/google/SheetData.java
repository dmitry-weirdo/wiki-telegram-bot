package com.dv.telegram.google;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SheetData {

    private List<RowData> rows;

    public SheetData() {
        rows = new ArrayList<>();
    }

    public void addRow(RowData row) {
        rows.add(row);
    }
}
