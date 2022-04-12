package com.dv.telegram;

import com.dv.telegram.data.CityChatsParser;
import com.dv.telegram.data.CommandsParser;
import com.dv.telegram.data.WikiPagesParser;
import com.dv.telegram.google.GoogleSheetReader;
import com.dv.telegram.google.WikiBotGoogleSheet;

import java.util.List;

public final class GoogleSheetLoader {

    private GoogleSheetLoader() {
    }

    public static GoogleSheetBotData readGoogleSheet(WikiBotConfig config) {
        WikiBotGoogleSheet wikiBotGoogleSheet = GoogleSheetReader.readGoogleSheetSafe(config);

        List<WikiPageData> wikiPages = WikiPagesParser.parseWikiPages(wikiBotGoogleSheet);
        List<CityChatData> cityChatsData = CityChatsParser.parseCityChats(wikiBotGoogleSheet);
        List<WikiBotCommandData> botCommands = CommandsParser.parseWikiBotCommands(wikiBotGoogleSheet);

        return new GoogleSheetBotData(wikiPages, cityChatsData, botCommands);
    }
}
