package com.dv.telegram.notion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class ChatParseResult {
    public final Optional<NotionCityChat> chat;
    public final List<String> errorMessages;

    public static ChatParseResult empty() {
        return new ChatParseResult(
            Optional.empty(),
            List.of()
        );
    }

    public static ChatParseResult error(String... errorMessages) {
        return new ChatParseResult(
            Optional.empty(),
            List.of(errorMessages)
        );
    }

    public static ChatParseResult correctChat(String url, String name) {
        return new ChatParseResult(
            Optional.of(
                new NotionCityChat(url, name)
            ),
            List.of()
        );
    }

    public boolean isEmpty() {
        return !hasErrors() && chat.isEmpty();
    }

    public boolean isCorrect() {
        return !hasErrors() && chat.isPresent();
    }

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
}
