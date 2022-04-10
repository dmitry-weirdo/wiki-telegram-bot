package com.dv.telegram.config;

import org.apache.commons.lang3.StringUtils;

public class NoAnswerReply implements BotSetting<String> {

    public static final String NAME = NoAnswerReply.class.getSimpleName();

    private String value;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return """
            Сообщение бота, которое выдаётся, если бот не нашёл ответа на сообщение пользователя.
            В параметр `{0}` будет заполняться имя бота.
            В параметр `{1}` будет заполняться сообщение пользователя, на которое бот не нашёл ответа.
            
            Если *ReplyWhenNoAnswer* выставлена в `false`, бот не будет выдавать никакого сообщения, если он не нашёл ответа.
            """;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        if (StringUtils.isBlank(value)) {
            throw new SettingValidationException("Сообщение бота в случае ненайденного ответа не может быть пустым.");
        }

        this.value = value;
    }
}
