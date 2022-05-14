package com.dv.telegram.config

sealed interface BotSetting<T> {
    val name: String // English name, no spaces

    val description: String // description, in Russian
    fun getValue(): T
    fun setValue(value: String)

    companion object {
        val allSettings = listOf(
            StartMessage(),
            DeleteBotCallMessageOnMessageReply(),
            BotTriggerMode(),
            ReplyWhenNoAnswer(),
            NoAnswerReply()
        )
    }
}
