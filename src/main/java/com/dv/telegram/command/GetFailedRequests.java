package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GetFailedRequests extends BasicBotCommand {

    @Override
    public String getName() {
        return GetFailedRequests.class.getSimpleName();
    }

    @Override
    public String getDescription() {
        return String.format("%s — получить список разных неуспешных вызовов бота с момента текущего запуска инстанса или с момента очистки этого списка.", getCommandText());
    }

    @Override
    public String getDefaultCommandName() {
        return "/getFailedRequests";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        List<String> failedRequestsLines = bot
            .getStatistics()
            .getFailedRequests()
            .stream()
            .map(failedRequest -> String.format("— %s", failedRequest))
            .toList();

        String totalLine = String.format("Разных неудачных запросов: %d", failedRequestsLines.size());

        List<String> responseLines = new ArrayList<>();
        responseLines.add(totalLine);
        responseLines.addAll(failedRequestsLines);

        return StringUtils.join(responseLines, "\n");
    }
}
