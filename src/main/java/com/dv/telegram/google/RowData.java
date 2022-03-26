package com.dv.telegram.google;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RowData {

    private List<String> cells;

    public RowData() {
        this.cells = new ArrayList<>();
    }

    public void addCell(String cell) {
        cells.add(cell);
    }
}
