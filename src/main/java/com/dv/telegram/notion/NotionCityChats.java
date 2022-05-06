package com.dv.telegram.notion;

import com.dv.telegram.CityChatData;
import com.dv.telegram.exception.CommandException;
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
    private static final String EXPECTED_URL_START = "https://";

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
        List<String> errors = new ArrayList<>();

        List<NotionCityChats> chats = cityChatsData
            .stream()
            .map(chat -> from(chat, errors))
            .filter(Optional::isPresent) // filter out empty lines (without city name)
            .map(Optional::get)
            .toList();

        if (!errors.isEmpty()) {
            throw new CommandException(errors);
        }

        // sort cities by name
        return chats
            .stream()
            .sorted(Comparator.comparing(NotionCityChats::getCityName))
            .toList();
    }

    public static Optional<NotionCityChats> from(CityChatData cityChatData, List<String> errors) {
        String cityName = cityChatData.getCityName();
        if (StringUtils.isBlank(cityName)) {
            // todo: error on empty city name + non-empty chats?
            log.warn("Empty city name.");
            return Optional.empty();
        }

        NotionCityChats chats = new NotionCityChats();
        chats.setCityName(cityName);

        for (String chatUrlAndName : cityChatData.getChats()) {
            ChatParseResult chatParseResult = parseChat(chatUrlAndName);

            if (chatParseResult.isEmpty()) { // no chat and no error -> just skip
                continue;
            }

            if (chatParseResult.hasErrors()) {
                errors.addAll(chatParseResult.errorMessages);
            }

            chatParseResult.chat.ifPresent(chats::addChat);
        }

        return Optional.of(chats);
    }

    public static ChatParseResult parseChat(String chatString) {
        if (StringUtils.isBlank(chatString)) {
            return ChatParseResult.empty();
        }

        String[] split;

        if (StringUtils.contains(chatString, CHAT_LINK_AND_NAME_SEPARATOR_1)) {
            split = chatString.split(CHAT_LINK_AND_NAME_SEPARATOR_1);
            if (split.length < 2) {
                return ChatParseResult.error(
                    emptyChatName(chatString)
                );
            }
        }
        else if (StringUtils.contains(chatString, CHAT_LINK_AND_NAME_SEPARATOR_2)) {
            split = chatString.split(CHAT_LINK_AND_NAME_SEPARATOR_2);
            if (split.length < 2) {
                return ChatParseResult.error(
                    emptyChatName(chatString)
                );
            }
        }
        else {
            log.warn(
                "City chat string \"{}\" does not contain neither separator \"{}\" nor separator \"{}\".",
                chatString,
                CHAT_LINK_AND_NAME_SEPARATOR_1,
                CHAT_LINK_AND_NAME_SEPARATOR_2
            );

            return ChatParseResult.error(
                noSeparatorInChatString(chatString)
            );
        }

        var url = split[0].trim();
        var name = split[1].trim();

        if (StringUtils.isBlank(url)) {
            return ChatParseResult.error(
                emptyChatUrl(chatString)
            );
        }

        if (!url.startsWith(EXPECTED_URL_START)) {
            log.warn("Url \"{}\" does not start with \"{}\".", url, EXPECTED_URL_START);

            return ChatParseResult.error(
                chatUrlDoesNotStartWithHttps(chatString, url)
            );
        }

        if (StringUtils.isBlank(name)) {
            log.warn("Chat name for chat with url \"{}\" is empty. Please check that the chat exists and add its name.", url);

            return ChatParseResult.error(
                emptyChatName(chatString)
            );
        }

        return ChatParseResult.correctChat(url, name);
    }

    private static String emptyChatUrl(String chatString) {
        return String.format("Описание чата \"%s\": пустой URL чата.", chatString);
    }

    private static String noSeparatorInChatString(String chatString) {
        return String.format(
            "Описание чата \"%s\" не содержит ни разделителя \"%s\", ни разделителя \"%s\".",
            chatString,
            CHAT_LINK_AND_NAME_SEPARATOR_1,
            CHAT_LINK_AND_NAME_SEPARATOR_2
        );
    }

    private static String chatUrlDoesNotStartWithHttps(String chatString, String url) {
        return String.format("Описание чата \"%s\": URL чата \"%s\" не начинается с \"%s\".", chatString, url, EXPECTED_URL_START);
    }

    private static String emptyChatName(String chatString) {
        return String.format("Описание чата \"%s\": пустое имя чата. Проверьте, что чат существует, и добавьте его название в описание чата.", chatString);
    }
}
