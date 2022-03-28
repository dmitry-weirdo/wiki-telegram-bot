package com.dv.telegram;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GoogleSheetBotData {
    private final List<WikiPageData> pages;
    private final List<CityChatData> cityChats;
    private final List<WikiBotCommandData> commands;
}
