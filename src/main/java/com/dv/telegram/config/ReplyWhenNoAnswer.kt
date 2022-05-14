package com.dv.telegram.config;

import lombok.Data;

@Data
public class ReplyWhenNoAnswer implements BotSetting<Boolean> {

    public static final String NAME = ReplyWhenNoAnswer.class.getSimpleName();

    private boolean value;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return """
            *true* — Если бот не нашёл ответа, он выдаст ответ, определённый настройкой *NoAnswerReply*.

            *false* — Если бот не нашёл ответа, он не будет отвечать.
            """;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = Boolean.parseBoolean(value);
    }
}
