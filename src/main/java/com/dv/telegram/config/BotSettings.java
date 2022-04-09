package com.dv.telegram.config;

import com.dv.telegram.WikiBotConfig;

import java.util.Map;

public class BotSettings {

    public final Map<String, BotSetting<?>> settings;

    public String startMessage;
    public boolean deleteBotCallMessageOnMessageReply;

    public static BotSettings create(WikiBotConfig config) {
        Map<String, BotSetting<?>> settingsMap = BotSettingUtils.fillSettingsMap(config);
        return new BotSettings(settingsMap);
    }

    public BotSettings(Map<String, BotSetting<?>> settings) {
        this.settings = settings;
        fillSettingCacheFields();
    }

    public void fillSettingCacheFields() {
        this.startMessage = getStartMessage();
        this.deleteBotCallMessageOnMessageReply = getDeleteBotCallMessageOnMessageReply();
    }

    public BotSetting<?> getBotSetting(String settingName) {
        return settings.get(settingName);
    }

    private String getStartMessage() {
        return getStringSetting(StartMessage.NAME);
    }

    private boolean getDeleteBotCallMessageOnMessageReply() {
        return getBooleanSetting(DeleteBotCallMessageOnMessageReply.NAME);
    }

    private String getStringSetting(String settingName) {
        BotSetting<?> setting = getSetting(settingName);
        return (String) setting.getValue();
    }

    private boolean getBooleanSetting(String settingName) {
        BotSetting<?> setting = getSetting(settingName);
        return (boolean) setting.getValue();
    }

    private BotSetting<?> getSetting(String settingName) {
        BotSetting<?> setting = settings.get(settingName);

        if (setting == null) {
            throw new SettingValidationException(String.format("No value for setting \"%s\".", settingName));
        }

        return setting;
    }
}
