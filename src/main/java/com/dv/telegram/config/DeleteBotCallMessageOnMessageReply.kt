package com.dv.telegram.config

class DeleteBotCallMessageOnMessageReply : BotSetting<Boolean> {

    companion object {
        // class simpleName cannot be const, see https://stackoverflow.com/questions/37182900/static-const-in-kotlin-from-java-class-name
        const val NAME: String = "DeleteBotCallMessageOnMessageReply"
    }

    private var value: Boolean = false

    override val name = NAME

    override val description = """
        *${true}* — Если сообщение к боту является реплаем, то оно будет удалено, и ответ бота будет реплаем на оригинальное сообщение.
        ❗Для того, чтобы удаление сообщения ботом работало в группе, бот должен быть в группе админом с правом удалять сообщения.

        *${false}* — Если сообщение к боту является реплаем, то оно НЕ будет удалено, и ответ бота будет реплаем на сообщение к боту.

        Если сообщение к боту не является реплаем, то это сообщение не будет удаляться вне зависимости от этой настройки.
        """.trimIndent()

    override fun getValue() = value

    override fun setValue(value: String?) {
        this.value = value.toBoolean()
    }
}
