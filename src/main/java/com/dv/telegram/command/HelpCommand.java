package com.dv.telegram.command;

import com.dv.telegram.WikiBot;

public class HelpCommand extends BasicBotCommand {

    @Override
    public String getName() {
        return HelpCommand.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s <commandName>` — получить помощь по команде с названием `<commandName>`.",
            bot.getBotName(),
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

        BotCommand botCommand = bot
            .getSpecialCommands()
            .getCommand(commandText);

        if (botCommand == null) {
            return unknownCommandResponse();
        }

        return String.format(
            "*%s*\n%s",
            botCommand.getCommandText(),
            botCommand.getDescription(bot)
        );
    }

    private String unknownCommandResponse() {
        return "Неизвестное имя команды.";
    }
}
