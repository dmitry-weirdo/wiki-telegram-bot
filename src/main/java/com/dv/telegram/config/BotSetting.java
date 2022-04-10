package com.dv.telegram.config;

import java.util.List;

public interface BotSetting<T> {
    String getName(); // english name, no spaces
    String getDescription(); // description, in russian

    T getValue();
    void setValue(String value);

    static List<BotSetting<?>> getAllSettings() {
        return List.of(
            new StartMessage(),
            new DeleteBotCallMessageOnMessageReply(),
            new BotTriggerMode(),
            new ReplyWhenNoAnswer(),
            new NoAnswerReply()
        );
    }
}
