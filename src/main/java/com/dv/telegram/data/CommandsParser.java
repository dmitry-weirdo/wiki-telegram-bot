package com.dv.telegram.data;

import com.dv.telegram.WikiBotCommandData;
import com.dv.telegram.google.RowData;
import com.dv.telegram.google.SheetData;
import com.dv.telegram.google.WikiBotGoogleSheet;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class CommandsParser {

    public static List<WikiBotCommandData> parseWikiBotCommands(WikiBotGoogleSheet sheet) {
        SheetData commandsSheet = sheet.getCommandsSheet();
        List<RowData> rows = commandsSheet.getRowsWithoutFirstRow();

        List<WikiBotCommandData> commands = new ArrayList<>();

        int rowNum = 1;

        for (RowData row : rows) {
            String answer = row.getCellOrBlank(0);
            String wordsString = row.getCellOrBlank(1);

            List<String> words = DataUtils.parseWords(wordsString);

            WikiBotCommandData command = new WikiBotCommandData(answer, wordsString, words);
            commands.add(command);

            log.info("Row {}: / {} / {}", rowNum, answer, words);
            rowNum++;
        }

        log.info("Total {} commands parsed.", commands.size());

        return commands;
    }
}
