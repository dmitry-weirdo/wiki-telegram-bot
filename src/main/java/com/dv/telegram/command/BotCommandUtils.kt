package com.dv.telegram.command;

import com.dv.telegram.WikiBotConfig;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public final class BotCommandUtils {

    private static final String USER_NAME_PREFIX = "@";

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

    public static Set<String> getBotAdmins(WikiBotConfig config) {
        List<String> botAdmins = config.getBotAdmins();

        return botAdmins
            .stream()
            .map(BotCommandUtils::normalizeUserName) // cut off "@" if it is present
            .sorted(Comparator.comparing(s -> s.toLowerCase(Locale.ROOT))) // prevent java's "big letters first" sorting
            .collect(Collectors.toCollection(LinkedHashSet::new)); // sort admins set by userName
    }

    public static String normalizeUserName(String userName) {
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("userName cannot be null or blank.");
        }

        if (userName.startsWith(USER_NAME_PREFIX)) { // cut off "@" if it is present
            return userName.substring(USER_NAME_PREFIX.length());
        }

        return userName;
    }

    public static String getClickableUserName(String userName) {
        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("userName cannot be null or blank.");
        }

        if (userName.startsWith(USER_NAME_PREFIX)) {
            return userName;
        }

        return String.format("%s%s", USER_NAME_PREFIX, userName); // append "@" if it is NOT present
    }
}
