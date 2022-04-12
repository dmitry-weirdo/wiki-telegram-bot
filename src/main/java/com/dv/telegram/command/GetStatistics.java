package com.dv.telegram.command;

import com.dv.telegram.BotStatistics;
import com.dv.telegram.WikiBot;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GetStatistics extends BasicBotCommand {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");

    @Override
    public String getName() {
        return GetStatistics.class.getSimpleName();
    }

    @Override
    public String getDescription(String botName) {
        return String.format(
            "`%s %s` — получить статистику по работе бота с момента текущего запуска инстанса.",
            botName,
            getCommandText()
        );
    }

    @Override
    public String getDefaultCommandName() {
        return "/getStats";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        BotStatistics statistics = bot.getStatistics();

        List<String> statisticsLines = List.of(
            getStatisticsLine("Время старта бота", statistics.startTime),
            getStatisticsLine("Успешных запросов", statistics.getSuccessfulRequestsCountWithPercentage()),
            getStatisticsLine("Неуспешных запросов", statistics.getFailedRequestsCountWithPercentage()),
            getStatisticsLine("Всего запросов", statistics.getTotalCallsWithPercentage()),
            getStatisticsLine("Вызовов специальных команд", statistics.specialCommandsCount),
            getStatisticsLine("Всего запросов (вместе со специальными командами)", statistics.getTotalCallsWithSpecialCommands())
        );

        return StringUtils.join(statisticsLines, "\n");
    }

    private String getStatisticsLine(String name, long count) {
        return String.format("— %s: %d", name, count);
    }

    private String getStatisticsLine(String name, String value) {
        return String.format("— %s: %s", name, value);
    }

    private String getStatisticsLine(String name, ZonedDateTime time) {
        return String.format("— %s: %s", name, dateTimeFormatter.format(time));
    }
}
