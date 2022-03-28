package com.dv.telegram.data;

import com.dv.telegram.CityChatData;
import com.dv.telegram.google.RowData;
import com.dv.telegram.google.SheetData;
import com.dv.telegram.google.WikiBotGoogleSheet;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class CityChatsParser {

    public static List<CityChatData> parseCityChats(WikiBotGoogleSheet sheet) {
        SheetData commandsSheet = sheet.getCityChatsSheet();
        List<RowData> rows = commandsSheet.getRowsWithoutFirstRow();

        List<CityChatData> chatsData = new ArrayList<>();

        int rowNum = 1;

        for (RowData row : rows) {
            String cityName = row.getCellOrBlank(0);

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

            CityChatData cityChat = new CityChatData(cityName, wordsString, words, chats);
            chatsData.add(cityChat);

            log.info("Row {}: / {} / {} / {}", rowNum, cityName, words, chats);
            log.info(cityChat.getChatsAnswer());
            rowNum++;
        }

        log.info("Total {} commands parsed.", chatsData.size());

        return chatsData;
    }
}
