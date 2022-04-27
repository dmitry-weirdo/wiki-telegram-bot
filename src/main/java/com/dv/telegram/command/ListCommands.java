package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ListCommands extends BasicBotCommand {

    @Override
    public String getName() {
        return ListCommands.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s` — получить список команд бота.",
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
        return "/listCommands";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        List<String> lines = new ArrayList<>();

        lines.add("Список команд бота:");

        for (BotCommand command : bot.getSpecialCommands().getCommands()) {
            lines.add(String.format(
                "— `%s %s`",
                bot.getBotName(),
                command.getCommandText()
            ));
        }

        lines.add(String.format(
            "Для получения справки по команде используйте команду%n`%s %s <commandName>`",
            bot.getBotName(),
            bot.getSpecialCommands().getHelpCommand().getCommandText()
        ));

        return StringUtils.join(lines, "\n\n");
    }
}
