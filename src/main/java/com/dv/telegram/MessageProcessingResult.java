package com.dv.telegram;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class MessageProcessingResult {

    public final boolean messageIsForTheBot;
    public final boolean isSpecialCommand;
    public final boolean useMarkdown;
    public final boolean answerIsFound;
    public final Optional<String> response;

    public boolean hasNoResponse() {
        return response.isEmpty();
    }

    public String getResponse() {
        return response
            .orElseThrow(
                () -> new IllegalStateException("getResponse called on an empty response.")
            );
    }

    public static MessageProcessingResult notForTheBot() {
        return new MessageProcessingResult(false, false, false, false, Optional.empty());
    }

    public static MessageProcessingResult specialCommand(Optional<String> response, boolean useMarkdown) {
        return new MessageProcessingResult(true, true, useMarkdown, true, response);
    }

    public static MessageProcessingResult answerFound(String response) {
        return new MessageProcessingResult(true, false, false, true, Optional.of(response));
    }

    public static MessageProcessingResult answerFound(Optional<String> response) {
        if (response.isEmpty()) {
            throw new IllegalArgumentException("For answerFound result, the response text must be present.");
        }

        return new MessageProcessingResult(true, false, false, true, response);
    }

    public static MessageProcessingResult answerNotFound(Optional<String> response) {
        return new MessageProcessingResult(true, false, false, false, response);
    }
}
