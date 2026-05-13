package com.dv.telegram.command

class BotContext {
    @PublishedApi
    internal val context: MutableMap<String, Any> = mutableMapOf()

    fun get(key: String): Any? {
        return context[key]
    }

    @JvmName("getTyped")
    inline fun <reified T> get(key: String): T? {
        val value = context[key]
        return value as? T
    }

    fun getOrFail(key: String): Any {
        return context[key]
            ?: throw IllegalStateException("Key '$key' not found in BotContext")
    }

    @JvmName("getOrFailTyped")
    inline fun <reified T> getOrFail(key: String): T {
        val value = context[key]
        return value as? T
            ?: throw IllegalStateException("Key '$key' not found in BotContext or value is not of type ${T::class.simpleName}")
    }

    fun isPresent(key: String): Boolean {
        return context.containsKey(key)
    }

    fun put(key: String, value: Any) {
        context[key] = value
    }
}
