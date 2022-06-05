package com.dv.telegram.config

import com.dv.telegram.WikiBotConfig

object BotSettingUtils {
    fun fillSettingsMap(config: WikiBotConfig): Map<String, BotSetting<*>> {
        return fillSettingsMap(config.settings)
    }

    fun fillSettingsMap(settings: Map<String?, String?>): Map<String, BotSetting<*>> {
        val botSettings = mutableMapOf<String, BotSetting<*>>()

        val allSettings = BotSetting.getAllSettings() // get new copy of all settings to fill

        for ((name, value) in settings) {
            if (name.isNullOrBlank()) {
                throw SettingValidationException("Setting name cannot be empty.")
            }

            if (value.isNullOrBlank()) {
                throw SettingValidationException("Setting value cannot be empty.")
            }

            val setting = getBotSetting(allSettings, name)
            setting.setValue(value)

            if (botSettings.containsKey(name)) {
                throw SettingValidationException("Duplicate setting: \"${name}\".")
            }

            botSettings[name] = setting
        }

        // todo: validate that all settings are present in the config
        return botSettings
    }

    private fun getBotSetting(allSettings: List<BotSetting<*>>, settingName: String): BotSetting<*> {
        return allSettings
            .firstOrNull { it.name == settingName }
            ?: throw SettingValidationException("Unknown setting name: \"${settingName}\".")
    }
}
