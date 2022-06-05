package com.dv.telegram.config

class BotTriggerMode : BotSetting<BotTriggerMode.Mode> {

    companion object {
        // class simpleName cannot be const, see https://stackoverflow.com/questions/37182900/static-const-in-kotlin-from-java-class-name
        const val NAME: String = "BotTriggerMode"
    }

    enum class Mode {
        ANY_SUBSTRING, // любая подстрока с именем бота, в том числе, внутри другого слова
        STRING_START, // только имя бота вначале сообщения
        FULL_WORD // только имя бота как отдельное слово
    }

    private var mode: Mode = Mode.FULL_WORD

    override val name = NAME

    override val description = """
        *${Mode.ANY_SUBSTRING.name}* — Вызов бота триггерит любая подстрока с его именем. Например, сообщение «клубника» триггернёт бота «Ника».
    
        *${Mode.STRING_START.name}* — Вызов бота триггерит только сообщение, которое начинается с имени бота.
    
        *${Mode.FULL_WORD.name}* — Вызов бота триггерит только сообщение, в котором имя бота указано как отдельное слово.
        
        Во всех случаях имя бота можно указывать в любом регистре.
        """.trimIndent()

    override fun getValue(): Mode = mode

    override fun setValue(value: String?) {
        if (value.isNullOrBlank()) {
            throw SettingValidationException("Mode value cannot be null or blank.")
        }

        try {
            mode = Mode.valueOf(value)
        }
        catch (e: IllegalArgumentException) {
            throw SettingValidationException("Incorrect Mode value: \"$value\".")
        }
    }
}
