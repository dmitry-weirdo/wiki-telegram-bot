package com.dv.telegram.command;

import com.dv.telegram.WikiBotConfig;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public final class BotCommandUtils {

    private BotCommandUtils() {
    }

    public static List<BotCommand> fillCommands(WikiBotConfig config) {
        List<BotCommand> allCommands = BotCommand.getAllCommands();

        Map<String, String> commandNames = config.getCommands();
        if (MapUtils.isEmpty(commandNames)) {
            return allCommands;
        }

        for (BotCommand command : allCommands) {
            String name = command.getName();

            String overriddenCommandName = commandNames.get(name);
            if (StringUtils.isNotBlank(overriddenCommandName)) {
                command.setCommandName(overriddenCommandName);
            }
        }

        return allCommands;
    }
}
