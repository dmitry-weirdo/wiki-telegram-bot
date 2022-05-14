package com.dv.telegram.config

class ReplyWhenNoAnswer : BotSetting<Boolean> {

    companion object {
        // class simpleName cannot be const, see https://stackoverflow.com/questions/37182900/static-const-in-kotlin-from-java-class-name
        const val NAME: String = "ReplyWhenNoAnswer"
    }

    private var value = false

    override val name = NAME

    override val description = """
        *${true}* — Если бот не нашёл ответа, он выдаст ответ, определённый настройкой *${NoAnswerReply.NAME}*.

        *${false}* — Если бот не нашёл ответа, он не будет отвечать.
        """.trimIndent()

    override fun getValue() = value

    override fun setValue(value: String?) {
        this.value = value.toBoolean()
    }
}
