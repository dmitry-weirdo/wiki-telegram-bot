package com.dv.telegram.data;

import com.dv.telegram.google.RowData;
import com.dv.telegram.google.SheetData;
import com.dv.telegram.google.WikiBotGoogleSheet;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class CountryChatsParser { // mostly the same as CityChatsParser

    public static List<CountryChatData> parseCountryChats(WikiBotGoogleSheet sheet) {
        SheetData commandsSheet = sheet.getCountryChatsSheet();
        List<RowData> rows = commandsSheet.getRowsWithoutFirstRow();

        List<CountryChatData> chatsData = new ArrayList<>();

        int rowNum = 1;

        for (RowData row : rows) {
            String countryName = row.getCellOrBlank(0);

            // words
            String wordsString = row.getCellOrBlank(1);
            List<String> words = DataUtils.parseWords(wordsString);

            List<String> chats = new ArrayList<>();
            for (int cellNum = 2; cellNum < row.getCells().size(); cellNum++) {
                String chat = row.getCellOrBlank(cellNum);
                if (StringUtils.isNotBlank(chat)) {
                    chats.add(chat);
                }
            }

            CountryChatData countryChat = new CountryChatData(countryName, wordsString, words, chats);
            chatsData.add(countryChat);

            log.info("Row {}: / {} / {} / {}", rowNum, countryName, words, chats);
            log.info(countryChat.getChatsAnswer());
            rowNum++;
        }

        log.info("Total {} commands parsed.", chatsData.size());

        return chatsData;
    }
}
