package com.dv.telegram.config

class StartMessage : BotSetting<String> {

    companion object {
        // class simpleName cannot be const, see https://stackoverflow.com/questions/37182900/static-const-in-kotlin-from-java-class-name
        const val NAME: String = "StartMessage"
    }

    private var value: String = ""

    override val name = NAME

    override val description = """
        Стартовое сообщение бота, выдаётся по команде `/start`.
        В параметр `{0}` будет заполняться имя бота.
        Можно использовать `\\n` как символ переноса строки.
        """.trimIndent()

    override fun getValue() = value

    override fun setValue(value: String?) {
        if (value.isNullOrBlank()) {
            throw SettingValidationException("Стартовое сообщение бота не может быть пустым.")
        }

        this.value = value
    }
}
