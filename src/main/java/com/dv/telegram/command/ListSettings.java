package com.dv.telegram.command;

import com.dv.telegram.WikiBot;
import com.dv.telegram.config.BotSetting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ListSettings extends BasicBotCommand {

    @Override
    public String getName() {
        return ListSettings.class.getSimpleName();
    }

    @Override
    public String getDescription(WikiBot bot) {
        return String.format(
            "`%s %s` — вывести список изменяемых в рантайме настроек бота.",
            bot.getBotName(),
            getCommandText()
        );
    }

    @Override
    public boolean useMarkdownInResponse() {
        return true;
    }

    @Override
    public String getDefaultCommandName() {
        return "/listSettings";
    }

    @Override
    public String getResponse(String text, WikiBot bot) {
        List<String> settingsLines = new ArrayList<>();

        for (BotSetting<?> setting : bot.getSettings().settings.values()) {
            settingsLines.add(String.format(
                "— *%s*:%n%s",
                setting.getName(),
                getSettingValueForMarkdown(setting)
            ));
        }

        return StringUtils.join(settingsLines, "\n\n");
    }
}
