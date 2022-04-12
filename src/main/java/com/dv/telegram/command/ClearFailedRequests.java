package com.dv.telegram.command;

import com.dv.telegram.BotStatistics;
import com.dv.telegram.config.BotSettings;

public class ClearFailedRequests extends BasicBotCommand {

    @Override
    public String getName() {
        return ClearFailedRequests.class.getSimpleName();
    }

    @Override
    public String getDescription() {
        return String.format("%s — очистить список разных неуспешных вызовов бота с момента текущего запуска инстанса или с момента очистки этого списка.", getCommandText());
    }

    @Override
    public String getDefaultCommandName() {
        return "/clearFailedRequests";
    }

    @Override
    public String getResponse(String text, BotSettings settings, BotStatistics statistics) {
        int clearedFailedRequestsCount = statistics.failedRequests.size();
        statistics.clearFailedRequests();
        return String.format("Список из %d неудачных запросов к боту очищен.", clearedFailedRequestsCount);
    }
}
