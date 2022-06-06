package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ListAdmins extends BasicBotCommand {

    @Override
    public String getName() {
        return ListAdmins.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s` — вывести список администраторов бота, которые имеют право запускать специальные команды из списка в `%s %s`.%n%nКоманда `%s` всегда доступна всем пользователям.",
            bot.getBotName(),
            getCommandText(),
            bot.getBotName(),
            bot.getSpecialCommands().getListCommands().getCommandText(),
            bot.getSpecialCommands().getStartCommand().getCommandText()
        );
    }

    @Override
    public boolean useMarkdownInResponse() {
        return true;
    }

    @Override
    public String getDefaultCommandName() {
        return "/listAdmins";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        List<String> adminLines = new ArrayList<>();

        for (String botAdmin : bot.getSpecialCommands().getBotAdmins()) {
            adminLines.add(String.format(
                "— %s",
                getSettingValueForMarkdown( // escape _ in user names like "dmitry_weirdo"
                    BotCommandUtils.getClickableUserName(botAdmin)
                )
            ));
        }

        return StringUtils.join(adminLines, "\n");
    }
}
