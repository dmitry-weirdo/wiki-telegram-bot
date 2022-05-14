package com.dv.telegram.config

class NoAnswerReply : BotSetting<String> {

    companion object {
        // class simpleName cannot be const, see https://stackoverflow.com/questions/37182900/static-const-in-kotlin-from-java-class-name
        const val NAME: String = "NoAnswerReply"
    }

    private var value: String = ""

    override val name = NAME

    override val description =  """
        Сообщение бота, которое выдаётся, если бот не нашёл ответа на сообщение пользователя.
        В параметр `{0}` будет заполняться имя бота.
        В параметр `{1}` будет заполняться сообщение пользователя, на которое бот не нашёл ответа.
        
        Если *${ReplyWhenNoAnswer.NAME}* выставлена в `${false}`, бот не будет выдавать никакого сообщения, если он не нашёл ответа.
        """.trimIndent()

    override fun getValue() = value

    override fun setValue(value: String?) {
        if (value.isNullOrBlank()) {
            throw SettingValidationException("Сообщение бота в случае ненайденного ответа не может быть пустым.")
        }

        this.value = value
    }
}
