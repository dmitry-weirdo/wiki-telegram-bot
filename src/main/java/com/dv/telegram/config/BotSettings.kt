package com.dv.telegram.config

import com.dv.telegram.WikiBotConfig

class BotSettings(val settings: Map<String, BotSetting<*>>) {

    companion object {
        @JvmStatic
        fun create(config: WikiBotConfig): BotSettings {
            val settingsMap = BotSettingUtils.fillSettingsMap(config)
            return BotSettings(settingsMap)
        }
    }

    // todo: I had to set values because calling fillSettingCacheFields is not recognized by the compiler
    var startMessage = ""
    var deleteBotCallMessageOnMessageReply = false
    var triggerMode: BotTriggerMode.Mode = BotTriggerMode.Mode.FULL_WORD
    var replyWhenNoAnswer = true
    var noAnswerReply = ""

    init { // will override all fields on class construction
        fillSettingCacheFields()
    }

    fun fillSettingCacheFields() {
        startMessage = getStartMessageValue()
        deleteBotCallMessageOnMessageReply = getDeleteBotCallMessageOnMessageReplyValue()
        triggerMode = getBotTriggerModeValue()
        replyWhenNoAnswer = getReplyWhenNoAnswerValue()
        noAnswerReply = getNoAnswerReplyValue()
    }

    fun getBotSetting(settingName: String): BotSetting<*>? {
        return settings[settingName]
    }

    private fun getStartMessageValue(): String {
        return getStringSetting(StartMessage.NAME)
    }

    private fun getDeleteBotCallMessageOnMessageReplyValue(): Boolean {
        return getBooleanSetting(DeleteBotCallMessageOnMessageReply.NAME)
    }

    private fun getBotTriggerModeValue(): BotTriggerMode.Mode {
        val setting = getSetting(BotTriggerMode.NAME)
        return setting.getValue() as BotTriggerMode.Mode
    }

    private fun getReplyWhenNoAnswerValue(): Boolean {
        return getBooleanSetting(ReplyWhenNoAnswer.NAME)
    }

    private fun getNoAnswerReplyValue(): String {
        return getStringSetting(NoAnswerReply.NAME)
    }

    private fun getStringSetting(settingName: String): String {
        val setting = getSetting(settingName)
        return setting.getValue() as String
    }

    private fun getBooleanSetting(settingName: String): Boolean {
        val setting = getSetting(settingName)
        return setting.getValue() as Boolean
    }

    private fun getSetting(settingName: String): BotSetting<*> {
        return settings[settingName]
            ?: throw SettingValidationException("No value for setting \"${settingName}\".")
    }
}
