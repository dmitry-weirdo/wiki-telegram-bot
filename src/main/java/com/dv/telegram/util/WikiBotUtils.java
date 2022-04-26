package com.dv.telegram.util;

import com.dv.telegram.WikiBotConfig;
import com.dv.telegram.WikiBotConfigs;
import org.apache.commons.lang3.StringUtils;

public final class WikiBotUtils {

    public static final String WIKI_BOT_CONFIG_FILE_PATH_ENV_NAME = "WIKI_BOT_CONFIG_FILE_PATH";

    private WikiBotUtils() {
    }

    public static WikiBotConfig readConfig() {
        String configFilePath = getConfigFilePath();

        WikiBotConfig config = JacksonUtils.parseConfig(configFilePath);
        config.fillDefaults();

        return config;
    }

    public static WikiBotConfigs readConfigs() {
        String configFilePath = getConfigFilePath();

        WikiBotConfigs configs = JacksonUtils.parseConfigs(configFilePath);
        configs.fillDefaults();

        return configs;
    }

    public static String getConfigFilePath() {
        return getEnvVariable(WIKI_BOT_CONFIG_FILE_PATH_ENV_NAME);
    }

    public static String getEnvVariable(String name) {
        String value = System.getenv(name);
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException(String.format("Environment variable %s is not set.", name));
        }

        return value;
    }
}
