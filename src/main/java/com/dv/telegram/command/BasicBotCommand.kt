package com.dv.telegram.command;

import com.dv.telegram.config.BotSetting;

public abstract class BasicBotCommand implements BotCommand {

    protected String commandName;

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public boolean useMarkdownInResponse() {
        return false;
    }

    public static String getSettingValueForMarkdown(BotSetting<?> botSetting) {
        return getSettingValueForMarkdown(
            botSetting.getValue().toString()
        );
    }

    public static String getSettingValueForMarkdown(String settingValue) {
        return settingValue.replaceAll("\\_", "\\\\_"); // for Markdown, escape "_" as "\_" to not fail sending the Telegram message
    }
}
