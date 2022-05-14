package com.dv.telegram.config;

import org.apache.commons.lang3.StringUtils;

public class StartMessage implements BotSetting<String> {

    public static final String NAME = StartMessage.class.getSimpleName();

    private String value;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return """
            Стартовое сообщение бота, выдаётся по команде `/start`.
            В параметр `{0}` будет заполняться имя бота.
            Можно использовать `\\n` как символ переноса строки.
            """;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        if (StringUtils.isBlank(value)) {
            throw new SettingValidationException("Стартовое сообщение бота не может быть пустым.");
        }

        this.value = value;
    }
}
