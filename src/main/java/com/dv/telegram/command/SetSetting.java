package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.config.BotSetting;

public class SetSetting extends BasicBotCommand {

    @Override
    public String getName() {
        return SetSetting.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s <settingName> <settingValue>` — установить значение настройки с названием `<settingName>` в значение `<settingValue>`.",
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
        return "/setSetting";
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

        String settingNameAndValue = text.substring(commandEndIndex).trim();

        String nameValueSeparator = " ";
        int separatorIndex = settingNameAndValue.indexOf(nameValueSeparator);

        if (separatorIndex < 0 || separatorIndex >= settingNameAndValue.length()) {
            return unknownSettingResponse(); // todo: probably another more concrete response
        }

        String settingName = settingNameAndValue.substring(0, separatorIndex).trim();
        String settingValue = settingNameAndValue.substring(separatorIndex).trim();

        BotSetting<?> botSetting = bot.getSettings().getBotSetting(settingName);
        if (botSetting == null) {
            return unknownSettingResponse();
        }

        try {
            botSetting.setValue(settingValue);
            bot.getSettings().fillSettingCacheFields();
        }
        catch (Exception e) {
            return String.format("Ошибка при установке настройки *%s* в значение\n%s", settingName, BasicBotCommand.getSettingValueForMarkdown(settingValue));
        }

        return String.format("*%s* установлена в значение\n%s", botSetting.getName(), BasicBotCommand.getSettingValueForMarkdown(botSetting));
    }

    private String unknownSettingResponse() {
        return "Неизвестное имя настройки.";
    }
}
