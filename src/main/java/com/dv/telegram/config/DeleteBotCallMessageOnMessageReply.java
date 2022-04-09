package com.dv.telegram.config;

import lombok.Data;

@Data
public class DeleteBotCallMessageOnMessageReply implements BotSetting<Boolean> {

    public static final String NAME = DeleteBotCallMessageOnMessageReply.class.getSimpleName();

    private boolean value;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return """
            *true* — Если сообщение к боту является реплаем, то оно будет удалено, и ответ бота будет реплаем на оригинальное сообщение.
            ❗Для того, чтобы удаление сообщения ботом работало в группе, бот должен быть в группе админом с правом удалять сообщения.

            *false* — Если сообщение к боту является реплаем, то оно НЕ будет удалено, и ответ бота будет реплаем на сообщение к боту.

            Если сообщение к боту не является реплаем, то это сообщение не будет удаляться вне зависимости от этой настройки.
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
