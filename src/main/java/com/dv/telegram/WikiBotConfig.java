package com.dv.telegram;

import lombok.Data;

import java.util.Map;

@Data
public class WikiBotConfig {
    public String botName;
    public String botToken;
    public String environmentName;

    // Google Sheets
    public String googleSheetsApiKey;

    public String googleSpreadsheetId;

    // see https://developers.google.com/sheets/api/guides/concepts
    // todo: use sheets by number instead if it is possible with A1 or R1C1 notation
    public String wikiPagesSheetName;
    public String cityChatsSheetName;
    public String countryChatsSheetName;
    public String commandsSheetName;

    public Map<String, String> commands; // special commands
    public Map<String, String> settings;
}
