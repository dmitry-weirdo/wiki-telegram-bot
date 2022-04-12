package com.dv.telegram.command;

import com.dv.telegram.WikiBot;

public class GetEnvironment extends BasicBotCommand {

    @Override
    public String getName() {
        return GetEnvironment.class.getSimpleName();
    }

    @Override
    public String getDescription() {
        return String.format(
            "%s — выдать окружение в которой запущен инстанс бота.",
            getCommandText()
        );
    }

    @Override
    public String getDefaultCommandName() {
        return "/getEnvironment";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        return String.format(
            "%s живёт здесь: %s.",
            bot.getBotName(),
            bot.getEnvironmentName()
        );
    }
}
