package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.WikiBotConfig;

import java.util.List;
import java.util.Optional;

public class BotSpecialCommands {

    private final List<BotCommand> commands;

    public static BotSpecialCommands create(WikiBotConfig config) {
        List<BotCommand> botCommands = BotCommandUtils.fillCommands(config);
        return new BotSpecialCommands(botCommands);
    }

    public BotSpecialCommands(List<BotCommand> commands) {
        this.commands = commands;
    }

    public boolean useMarkdownInResponse(String text) {
        return commands
            .stream()
            .anyMatch(command ->
                command.textContainsCommand(text)
                && command.useMarkdownInResponse()
            );
    }

    public Optional<String> getResponse(String text, WikiBot bot) {
        Optional<BotCommand> matchingCommandOptional = commands
            .stream()
            .filter(command -> command.textContainsCommand(text))
            .findFirst();

        if (matchingCommandOptional.isEmpty()) {
            return Optional.empty();
        }

        String response = matchingCommandOptional
            .get()
            .getResponse(text, bot);

        return Optional.of(response);
    }
}
