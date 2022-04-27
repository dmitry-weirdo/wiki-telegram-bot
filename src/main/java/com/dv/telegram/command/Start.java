package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.config.StartMessage;

import java.text.MessageFormat;

public class Start extends BasicBotCommand {

    @Override
    public String getName() {
        return Start.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s` — выдать приветственное сообщение бота. Сообщение определяется настройкой *%s*.%n%nКоманда также работает без имени бота: `%s`.",
            bot.getBotName(),
            getCommandText(),
            StartMessage.NAME,
            getCommandText()
        );
    }

    @Override
    public String getDefaultCommandName() {
        return "/start";
    }

    @Override
    public void setCommandName(String commandName) { // /start is a standard command, cannot be overridden
        throw new UnsupportedOperationException(String.format(
            "Cannot override %s command name.",
            getDefaultCommandName()
        ));
    }

    @Override
    public boolean requiresBotAdminRights() {
        return false; // /start command should be available for non-admin users too
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        return MessageFormat.format(
            bot.getSettings().startMessage,
            bot.getBotName()
        );
    }
}
