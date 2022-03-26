package com.dv.telegram.google;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WikiBotGoogleSheet { // only contains strings from the Google Sheet, not semantically parsed
    private SheetData wikiPagesSheet;
    private SheetData cityChatsSheet;
    private SheetData commandsSheet;
}
