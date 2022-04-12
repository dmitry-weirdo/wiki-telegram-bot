package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.config.BotSetting;

public class GetSetting extends BasicBotCommand {

    @Override
    public String getName() {
        return GetSetting.class.getSimpleName();
    }

    @Override
    public String getDescription() {
        return String.format("%s <settingName> — получить значение настройки с названием <settingName>.", getCommandText());
    }

    @Override
    public boolean useMarkdownInResponse() {
        return true;
    }

    @Override
    public String getDefaultCommandName() {
        return "/getSetting";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        int commandStartIndex = text.indexOf(getCommandText());
        if (commandStartIndex < 0) {
            return unknownSettingResponse();
        }

        int commandEndIndex = commandStartIndex + getCommandText().length();
        if (commandEndIndex >= text.length()) {
            return unknownSettingResponse();
        }

        String settingName = text.substring(commandEndIndex).trim();

        BotSetting<?> botSetting = bot.getSettings().getBotSetting(settingName);
        if (botSetting == null) {
            return unknownSettingResponse();
        }

        String value = BasicBotCommand.getSettingValueForMarkdown(botSetting);
        return String.format("*%s*%n%s", botSetting.getName(), value);
    }

    private String unknownSettingResponse() {
        return "Неизвестное имя настройки.";
    }
}
