package com.dv.telegram.exception;

import java.util.List;

public class CommandException extends RuntimeException {

    private final List<String> errorMessages;

    public CommandException(String message) {
        super(message);
        errorMessages = List.of(message);
    }

    public CommandException(List<String> messages) {
        super(messages.get(0));
        errorMessages = messages;
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
        errorMessages = List.of(message);
    }

    public CommandException(List<String> messages, Throwable cause) {
        super(messages.get(0), cause);
        errorMessages = messages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
