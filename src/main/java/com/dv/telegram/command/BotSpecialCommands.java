package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.WikiBotConfig;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BotSpecialCommands {

    private final Set<String> botAdmins;
    private final List<BotCommand> commands;
    private final HelpCommand helpCommand;

    public static BotSpecialCommands create(WikiBotConfig config) {
        Set<String> botAdmins = BotCommandUtils.getBotAdmins(config);

        List<BotCommand> botCommands = BotCommandUtils.fillCommands(config);
        return new BotSpecialCommands(botAdmins, botCommands);
    }

    public BotSpecialCommands(Set<String> botAdmins, List<BotCommand> commands) {
        this.botAdmins = botAdmins;
        this.commands = commands;

        this.helpCommand = (HelpCommand) commands
            .stream()
            .filter(HelpCommand.class::isInstance)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No HelpCommand in the list of commands."));
    }

    public List<BotCommand> getCommands() {
        return commands;
    }

    public HelpCommand getHelpCommand() {
        return helpCommand;
    }

    public Optional<BotCommand> getCommand(String commandText) {
        return commands
            .stream()
            .filter(command -> command.getCommandText().equals(commandText))
            .findFirst();
    }

    public boolean useMarkdownInResponse(String text) {
        return commands
            .stream()
            .anyMatch(command ->
                command.textContainsCommand(text)
                && command.useMarkdownInResponse()
            );
    }

    public SpecialCommandResponse getResponse(String text, String userName, WikiBot bot) {
        boolean userHasBotAdminRights = userHasRightsToExecuteSpecialCommands(userName);

        Optional<BotCommand> matchingCommandOptional = commands
            .stream()
            .filter(command ->
                   (userHasBotAdminRights || !command.requiresBotAdminRights())
                && command.textContainsCommand(text)
            )
            .findFirst();

        if (matchingCommandOptional.isEmpty()) {
            return SpecialCommandResponse.noResponse();
        }

        BotCommand command = matchingCommandOptional.get();
        String response = command.getResponse(text, bot);
        boolean useMarkdownInResponse = command.useMarkdownInResponse();

        return SpecialCommandResponse.withResponse(response, useMarkdownInResponse);
    }

    private boolean userHasRightsToExecuteSpecialCommands(String userName) {
        return botAdmins.contains(userName);
    }
}
