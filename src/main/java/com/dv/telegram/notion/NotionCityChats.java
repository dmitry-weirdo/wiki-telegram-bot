package com.dv.telegram.notion;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NotionCityChats {
    private String cityName;
    private List<NotionCityChat> chats = new ArrayList<>();

    public void addChat(NotionCityChat chat) {
        chats.add(chat);
    }

    public void addChat(String url, String name) {
        addChat(
            new NotionCityChat(url, name)
        );
    }
}
