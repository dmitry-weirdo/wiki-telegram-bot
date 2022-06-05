package com.dv.telegram;

import com.dv.telegram.command.SpecialCommandResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Log4j2
public class WikiBotMessageProcessor {

    private static final String START_COMMAND = "/start";

    private final WikiBot wikiBot;
    private final String botName;
    private final String botNameLowerCase;
    private final Pattern botNameWordPattern;

    public WikiBotMessageProcessor(WikiBot wikiBot) {
        this.wikiBot = wikiBot;

        this.botName = wikiBot.getBotName();
        this.botNameLowerCase = botName.toLowerCase(Locale.ROOT);
        this.botNameWordPattern = getBotNameFullWordPattern(botName);
    }

    public static Pattern getBotNameFullWordPattern(String botName) {
        String botNameRegex = String.format("(?i).*\\b%s\\b.*", botName);
        int botNamePatternFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL;
        return Pattern.compile(botNameRegex, botNamePatternFlags); // see https://stackoverflow.com/a/43738714/8534088
    }

    MessageProcessingResult processMessage(String text, String userName) { // non-private for testing
        if (StringUtils.isBlank(text)) {
            return MessageProcessingResult.notForTheBot();
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        if (!messageIsForTheBot(lowerText)) { // only work when bot is mentioned by name
            return MessageProcessingResult.notForTheBot();
        }

        // special commands - not configured in the Google Sheet
        SpecialCommandResponse specialCommandResponse = wikiBot.getSpecialCommands().getResponse(text, userName, wikiBot);
        if (specialCommandResponse.hasResponse()) { // special command received -> return response for the special command
            return MessageProcessingResult.specialCommand(specialCommandResponse.response, specialCommandResponse.useMarkdownInResponse);
        }

        // normal commands - configured in the Google Sheet
        String commandsAnswerText = wikiBot.getCommands().getResponseText(lowerText);
        if (StringUtils.isNotBlank(commandsAnswerText)) { // matching command found -> only handle the command
            return MessageProcessingResult.answerFound(commandsAnswerText);
        }

        List<String> answers = Arrays.asList( // List.of() fails on null values!
            wikiBot.getPages().getResponseText(lowerText), // wiki pages - configured in the Google Sheet
            wikiBot.getCityChats().getResponseText(lowerText), // city chats - configured in the Google Sheet
            wikiBot.getCountryChats().getResponseText(lowerText) // country chats - configured in the Google Sheet
        );

        return processMessage(text, answers);
    }

    private boolean messageIsForTheBot(String lowerText) {
        if (lowerText.equals(START_COMMAND)) { // special case: /start command without bot name
            return true;
        }

        return switch (wikiBot.getSettings().getTriggerMode()) { // check whether the message contains the bot name
            case ANY_SUBSTRING -> lowerText.contains(botNameLowerCase);
            case STRING_START -> lowerText.startsWith(botNameLowerCase);
            case FULL_WORD -> botNameWordPattern.matcher(lowerText).matches();
        };
    }

    private MessageProcessingResult processMessage(String text, List<String> answerOptionals) {
        List<String> answers = answerOptionals
            .stream()
            .filter(StringUtils::isNotBlank)
            .toList();

        if (answers.isEmpty()) { // no answers found
            Optional<String> noResultResponse = getNoResultResponse(text);
            return MessageProcessingResult.answerNotFound(noResultResponse);
        }

        String combinedAnswers = StringUtils.join(answers, "\n\n");
        return MessageProcessingResult.answerFound(combinedAnswers);
    }

    private Optional<String> getNoResultResponse(String text) {
        log.info("Unknown command for the bot: {}", text);

        if (wikiBot.getSettings().getReplyWhenNoAnswer()) { // reply on no answer
            String noResultAnswer = getNoResultAnswer(text);
            return Optional.of(noResultAnswer);
        }

        // no reply on no answer
        return Optional.empty();
    }

    String getNoResultAnswer(String text) { // not private for testing
        return MessageFormat.format(
            wikiBot.getSettings().getNoAnswerReply(),
            botName,
            text
        );
    }
}
