package com.dv.telegram.command;

import com.dv.telegram.WikiBot;

public class ReloadFromGoogleSheet extends BasicBotCommand {

    @Override
    public String getName() {
        return ReloadFromGoogleSheet.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s` — перезагрузить конфигурацию бота из Google Sheet.",
            bot.getBotName(),
            getCommandText()
        );
    }

    @Override
    public String getDefaultCommandName() {
        return "/gs-reload-5150"; // "secret" name to not be guessed by the user
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        boolean reloadedSuccessful = bot.reloadBotDataFromGoogleSheet();

        return reloadedSuccessful
            ? "Данные бота успешно загружены из Google Sheet."
            : "При загрузке данных из Google Sheet произошла ошибка."
        ;
    }
}
