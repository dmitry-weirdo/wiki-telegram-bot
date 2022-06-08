package com.dv.telegram.util

import com.dv.telegram.WikiBotConfig
import com.dv.telegram.WikiBotConfigs

object WikiBotUtils {
    private const val WIKI_BOT_CONFIG_FILE_PATH_ENV_NAME = "WIKI_BOT_CONFIG_FILE_PATH"

    fun readConfig(): WikiBotConfig {
        return JacksonUtils.parseConfig(configFilePath)
    }

    fun readConfigs(): WikiBotConfigs {
        return JacksonUtils.parseConfigs(configFilePath)
    }

    private val configFilePath: String
        get() = getEnvVariable(WIKI_BOT_CONFIG_FILE_PATH_ENV_NAME)

    fun getEnvVariable(name: String): String {
        val value = System.getenv(name)

        check(!value.isNullOrBlank()) { "Environment variable $name is not set." } // check will throw ISE if check not passed

        return value
    }
}
