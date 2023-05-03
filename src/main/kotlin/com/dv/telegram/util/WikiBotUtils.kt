package com.dv.telegram.util

object WikiBotUtils {
    private const val WIKI_BOT_CONFIG_FILE_PATH_ENV_NAME = "WIKI_BOT_CONFIG_FILE_PATH"

    fun readConfig() = JacksonUtils.parseConfig(configFilePath)

    fun readConfigs() = JacksonUtils.parseConfigs(configFilePath)

    private val configFilePath: String
        get() = getEnvVariable(WIKI_BOT_CONFIG_FILE_PATH_ENV_NAME)

    fun getEnvVariable(name: String): String {
        val value = System.getenv(name)

        // check will throw ISE if the check has not passed
        check(!value.isNullOrBlank()) { "Environment variable $name is not set." }

        return value
    }
}
