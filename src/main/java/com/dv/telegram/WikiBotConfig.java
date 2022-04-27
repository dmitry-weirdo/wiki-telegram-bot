package com.dv.telegram;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
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

    public List<String> botAdmins; // Telegram users that are allowed to execute the special commands

    public Map<String, String> commands; // special commands
    public Map<String, String> settings;

    public void fillDefaults() {
        wikiPagesSheetName = getDefault(wikiPagesSheetName, "Страницы вики и ключевые слова");
        cityChatsSheetName = getDefault(cityChatsSheetName, "Список чатов по городам");
        countryChatsSheetName = getDefault(countryChatsSheetName, "Список чатов по странам");
        commandsSheetName = getDefault(commandsSheetName, "Список болталки");
    }

    private static String getDefault(String value, String defaultValue) {
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }
}
