package com.dv.telegram.command;

import com.dv.telegram.WikiBot;

import java.util.Optional;

public class HelpCommand extends BasicBotCommand {

    @Override
    public String getName() {
        return HelpCommand.class.getSimpleName();
    }

    @Override
    public String getDescription(String botName) {
        return String.format(
            "`%s %s <commandName>` — получить помощь по команде с названием `<commandName>`.",
            botName,
            getCommandText()
        );
    }

    @Override
    public boolean useMarkdownInResponse() {
        return true;
    }

    @Override
    public String getDefaultCommandName() {
        return "/helpCommand";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        int commandStartIndex = text.indexOf(getCommandText());
        if (commandStartIndex < 0) {
            return unknownCommandResponse();
        }

        int commandEndIndex = commandStartIndex + getCommandText().length();
        if (commandEndIndex >= text.length()) {
            return unknownCommandResponse();
        }

        String commandText = text.substring(commandEndIndex).trim();

        Optional<BotCommand> botCommandOptional = bot
            .getSpecialCommands()
            .getCommand(commandText);

        if (botCommandOptional.isEmpty()) {
            return unknownCommandResponse();
        }

        BotCommand botCommand = botCommandOptional.get();

        return String.format(
            "*%s*%n%s",
            botCommand.getCommandText(),
            botCommand.getDescription(bot.getBotName())
        );
    }

    private String unknownCommandResponse() {
        return "Неизвестное имя команды.";
    }
}
