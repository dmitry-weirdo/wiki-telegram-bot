package com.dv.telegram.config;

import com.dv.telegram.WikiBotConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BotSettingUtils {

    private BotSettingUtils() {
    }

    public static Map<String, BotSetting<?>> fillSettingsMap(WikiBotConfig config) {
        return fillSettingsMap(config.getSettings());
    }

    public static Map<String, BotSetting<?>> fillSettingsMap(Map<String, String> settings) {
        List<BotSetting<?>> allSettings = BotSetting.getAllSettings();

        Map<String, BotSetting<?>> botSettings = new HashMap<>();

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            if (StringUtils.isBlank(name)) {
                throw new SettingValidationException("Setting name cannot be empty.");
            }

            if (StringUtils.isBlank(value)) {
                throw new SettingValidationException("Setting value cannot be empty.");
            }

            BotSetting<?> setting = getBotSetting(allSettings, name);
            setting.setValue(value);

            if (botSettings.containsKey(name)) {
                throw new SettingValidationException(String.format("Duplicate setting: \"%s\".", name));
            }

            botSettings.put(name, setting);
        }

        // todo: validate that all settings are present in the config

        return botSettings;
    }

    private static BotSetting<?> getBotSetting(List<BotSetting<?>> allSettings, String settingName) {
        return allSettings
            .stream()
            .filter(setting -> setting.getName().equals(settingName))
            .findFirst()
            .orElseThrow(() -> new SettingValidationException(String.format("Unknown setting name: \"%s\".", settingName)));
    }
}
