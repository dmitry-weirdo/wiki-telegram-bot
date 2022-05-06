package com.dv.telegram.notion;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NotionCityChatsImportResult {
    public String pageId;
    public String pageTitle;
    public String toggleHeading1Text;
    public int totalCities;
    public int totalChats;
}
