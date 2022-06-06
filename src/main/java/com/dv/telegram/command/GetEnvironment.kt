package com.dv.telegram.command;

import com.dv.telegram.WikiBot;

public class GetEnvironment extends BasicBotCommand {

    @Override
    public String getName() {
        return GetEnvironment.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s` — выдать окружение, в котором запущен инстанс бота.",
            bot.getBotName(),
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
