package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.exception.CommandException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public interface BotCommand {

    String getName(); // english name, no spaces
    String getDescription(WikiBot bot); // description, in Russian
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

    default boolean requiresBotAdminRights() {
        return true;
    }

    String getResponse(String text, WikiBot bot);

    default boolean textContainsCommand(String text) {
        return text.contains(
            getCommandText()
        );
    }

    default String errorResponse(CommandException e) {
        List<String> allErrors = new ArrayList<>();

        // no bold markdown in the header, because we can return the user input data in the error messages
        allErrors.add("При выполнении команды возникли следующие ошибки:");

        e.getErrorMessages().forEach(error ->
            allErrors.add(
                String.format("— %s%n", error)
            )
        );

        return StringUtils.join(allErrors, "\n");
    }

    default String unknownErrorResponse(Exception e) {
        return "При выполнении команды произошла неизвестная ошибка";
    }

    static List<BotCommand> getAllCommands() {
        return List.of(
            // commands
            new HelpCommand(), // first command to be found since it documents the other commands
            new ListCommands(),
            new ListAdmins(),

            // basic commands
            new Start(),
            new GetEnvironment(),
            new ReloadFromGoogleSheet(),

            // Notion
            new CityChatsValidate(),
            new CityChatsExportToNotion(),

            // settings
            new ListSettings(),
            new HelpSetting(),
            new GetSetting(),
            new SetSetting(),

            // statistics
            new GetStatistics(),
            new GetFailedRequests(),
            new ClearFailedRequests()
        );
    }
}
