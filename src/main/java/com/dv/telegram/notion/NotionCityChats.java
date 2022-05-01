package com.dv.telegram.notion;

import com.dv.telegram.CityChatData;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
@Log4j2
public class NotionCityChats {
    private static final String CHAT_LINK_AND_NAME_SEPARATOR_1 = " — ";
    private static final String CHAT_LINK_AND_NAME_SEPARATOR_2 = " - ";

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

    public static List<NotionCityChats> from(List<CityChatData> cityChatsData) {
        List<NotionCityChats> chats = cityChatsData
            .stream()
            .map(NotionCityChats::from)
            .toList();

        return chats
            .stream()
            .sorted(Comparator.comparing(NotionCityChats::getCityName))
            .toList();
    }

    public static NotionCityChats from(CityChatData cityChatData) {
        NotionCityChats chats = new NotionCityChats();
        chats.setCityName(cityChatData.getCityName());

        for (String chatUrlAndName : cityChatData.getChats()) {
            Optional<NotionCityChat> notionCityChat = parseChat(chatUrlAndName);
            notionCityChat.ifPresent(chats::addChat);
        }

        return chats;
    }

    public static Optional<NotionCityChat> parseChat(String chatString) {
        if (StringUtils.isBlank(chatString)) {
            return Optional.empty();
        }

        String[] split;

        if (StringUtils.contains(chatString, CHAT_LINK_AND_NAME_SEPARATOR_1)) {
            split = chatString.split(CHAT_LINK_AND_NAME_SEPARATOR_1);
            if (split.length < 2) {
                return Optional.empty();
            }
        }
        else if (StringUtils.contains(chatString, CHAT_LINK_AND_NAME_SEPARATOR_2)) {
            split = chatString.split(CHAT_LINK_AND_NAME_SEPARATOR_2);
            if (split.length < 2) {
                return Optional.empty();
            }
        }
        else {
            log.warn(
                "City chat string \"{}\" does not contain neither separator \"{}\" nor separator \"{}\".",
                chatString,
                CHAT_LINK_AND_NAME_SEPARATOR_1,
                CHAT_LINK_AND_NAME_SEPARATOR_2
            );

            return Optional.empty();
        }

        var url = split[0];
        var name = split[1];

        if (StringUtils.isBlank(url)) {
            return Optional.empty();
        }

        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }

        return Optional.of(
            new NotionCityChat(url, name)
        );
    }
}
