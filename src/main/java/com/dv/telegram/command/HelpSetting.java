package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.config.BotSetting;

public class HelpSetting extends BasicBotCommand {

    @Override
    public String getName() {
        return HelpSetting.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s <settingName>` — получить помощь по настройке с названием `<settingName>`.",
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
        return "/helpSetting";
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

        return String.format("*%s*\n%s", botSetting.getName(), botSetting.getDescription());
    }

    private String unknownSettingResponse() {
        return "Неизвестное имя настройки.";
    }
}
