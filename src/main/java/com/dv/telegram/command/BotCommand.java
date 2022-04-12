package com.dv.telegram.command;

import com.dv.telegram.BotStatistics;
import com.dv.telegram.config.BotSettings;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public interface BotCommand {

    String getName(); // english name, no spaces
    String getDescription(); // description, in russian
    boolean useMarkdownInResponse();

    String getDefaultCommandName(); // default command when it is not overridden by config
    String getCommandName();
    void setCommandName(String commandName);

    default String getCommandText() {
        if (StringUtils.isBlank(getCommandName())) {
            return getDefaultCommandName();
        }

        return getCommandName();
    }

    String getResponse(String text, BotSettings settings, BotStatistics statistics);

    default boolean textContainsCommand(String text) {
        return text.contains(
            getCommandText()
        );
    }

    static List<BotCommand> getAllCommands() {
        return List.of(
            // todo: commands (ListCommands, HelpCommand)

            // settings
            new ListSettings(),
            new HelpSetting(),
            new GetSetting(),
            new SetSetting(),

            // statistics
            new GetStatistics()
        );
    }
}
