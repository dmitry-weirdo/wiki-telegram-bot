package com.dv.telegram.config;

import lombok.Data;

@Data
public class BotTriggerMode implements BotSetting<BotTriggerMode.Mode> {

    public static final String NAME = BotTriggerMode.class.getSimpleName();

    public enum Mode {
        ANY_SUBSTRING, // любая подстрока с именем бота, в том числе, внутри другого слова
        STRING_START, // только имя бота вначале сообщения
        FULL_WORD // только имя бота как отдельное слово
    }

    private Mode mode;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return """
            *ANY_SUBSTRING* — Вызов бота триггерит любая подстрока с его именем. Например, сообщение «клубника» триггернёт бота «Ника».

            *STRING_START* — Вызов бота триггерит только сообщение, которое начинается с имени бота.

            *FULL_WORD* — Вызов бота триггерит только сообщение, в котором имя бота указано как отдельное слово.
            
            Во всех случаях имя бота можно указывать в любом регистре.
            """;
    }

    @Override
    public Mode getValue() {
        return mode;
    }

    @Override
    public void setValue(String value) {
        try {
            mode = Mode.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            throw new SettingValidationException(String.format("Incorrect Mode value: \"%s\".", value));
        }
    }
}
