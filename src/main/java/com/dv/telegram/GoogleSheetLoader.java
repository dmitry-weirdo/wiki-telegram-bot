package com.dv.telegram;

import com.dv.telegram.data.CityChatsParser;
import com.dv.telegram.data.CommandsParser;
import com.dv.telegram.data.CountryChatsParser;
import com.dv.telegram.data.WikiPagesParser;
import com.dv.telegram.exception.CommandException;
import com.dv.telegram.google.GoogleSheetReader;
import com.dv.telegram.google.WikiBotGoogleSheet;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public final class GoogleSheetLoader {

    private GoogleSheetLoader() {
    }

    public static GoogleSheetBotData readGoogleSheet(WikiBotConfig config) {
        try {
            log.info("Loading bot data from the Google Sheet...");
            GoogleSheetBotData botData = reloadGoogleSheetUnsafe(config);
            log.info("Bot data successfully reloaded from the Google Sheet.");

            return botData;
        }
        catch (Exception e) {
            log.error("Error when loading bot data from the Google Sheet", e);
            throw new CommandException("При загрузке данных из Google Sheet произошла ошибка.");
        }
    }

    private static GoogleSheetBotData reloadGoogleSheetUnsafe(WikiBotConfig config) {
        WikiBotGoogleSheet wikiBotGoogleSheet = GoogleSheetReader.readGoogleSheetSafe(config);

        List<WikiPageData> wikiPages = WikiPagesParser.parseWikiPages(wikiBotGoogleSheet);
        List<CityChatData> cityChatsData = CityChatsParser.parseCityChats(wikiBotGoogleSheet);
        List<CountryChatData> countryChatsData = CountryChatsParser.parseCountryChats(wikiBotGoogleSheet);
        List<WikiBotCommandData> botCommands = CommandsParser.parseWikiBotCommands(wikiBotGoogleSheet);

        return new GoogleSheetBotData(
            wikiPages,
            cityChatsData,
            countryChatsData,
            botCommands
        );
    }
}
