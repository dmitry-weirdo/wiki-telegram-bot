package com.dv.telegram.command;

import com.dv.telegram.WikiBot;

public class ClearFailedRequests extends BasicBotCommand {

    @Override
    public String getName() {
        return ClearFailedRequests.class.getSimpleName();
    }

    @Override
    public String getDescription(String botName) {
        return String.format(
            "`%s %s` — очистить список разных неуспешных вызовов бота с момента текущего запуска инстанса или с момента очистки этого списка.",
            botName,
            getCommandText()
        );
    }

    @Override
    public String getDefaultCommandName() {
        return "/clearFailedRequests";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        int clearedFailedRequestsCount = bot.getStatistics().failedRequests.size();
        bot.getStatistics().clearFailedRequests();
        return String.format("Список из %d неудачных запросов к боту очищен.", clearedFailedRequestsCount);
    }
}
