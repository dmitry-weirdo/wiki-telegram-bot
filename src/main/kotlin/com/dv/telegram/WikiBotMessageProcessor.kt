package com.dv.telegram

import com.dv.telegram.config.BotTriggerMode
import org.apache.logging.log4j.kotlin.Logging
import java.text.MessageFormat
import java.util.regex.Pattern

class WikiBotMessageProcessor(private val wikiBot: WikiBot) : Logging {
    private val botName = wikiBot.botName
    private val botNameLowerCase = botName.lowercase()
    private val botNameWordPattern = getBotNameFullWordPattern(botName)

    companion object {
        private const val START_COMMAND = "/start"

        fun getBotNameFullWordPattern(botName: String): Pattern {
            val botNameRegex = "(?i).*\\b$botName\\b.*"
            val botNamePatternFlags = Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE or Pattern.DOTALL

            return Pattern.compile(botNameRegex, botNamePatternFlags) // see https://stackoverflow.com/a/43738714/8534088
        }
    }

    fun processMessage(text: String, userName: String): MessageProcessingResult { // non-private for testing
        if (text.isBlank()) {
            return MessageProcessingResult.notForTheBot()
        }

        val lowerText = text.lowercase()

        if (!messageIsForTheBot(lowerText)) { // only work when bot is mentioned by name
            return MessageProcessingResult.notForTheBot()
        }

        // special commands - not configured in the Google Sheet
        val specialCommandResponse = wikiBot.specialCommands.getResponse(text, userName, wikiBot)
        if (specialCommandResponse.hasResponse()) { // special command received -> return response for the special command
            return MessageProcessingResult.specialCommand(
                specialCommandResponse.response,
                specialCommandResponse.useMarkdownInResponse
            )
        }

        // normal commands - configured in the Google Sheet
        val commandsAnswerText = wikiBot.commands.getResponseText(lowerText)
        if (commandsAnswerText?.isNotBlank() == true) { // matching command found -> only handle the command
            return MessageProcessingResult.answerFound(commandsAnswerText)
        }

        val answers = listOf(
            wikiBot.pages.getResponseText(lowerText), // wiki pages - configured in the Google Sheet
            wikiBot.cityChats.getResponseText(lowerText), // city chats - configured in the Google Sheet
            wikiBot.countryChats.getResponseText(lowerText) // country chats - configured in the Google Sheet
        )

        return processMessage(text, answers)
    }

    private fun messageIsForTheBot(lowerText: String): Boolean {
        if (lowerText == START_COMMAND) { // special case: /start command without bot name
            return true
        }

        return when (wikiBot.settings.triggerMode) {
            BotTriggerMode.Mode.ANY_SUBSTRING -> lowerText.contains(botNameLowerCase)
            BotTriggerMode.Mode.STRING_START -> lowerText.startsWith(botNameLowerCase)
            BotTriggerMode.Mode.FULL_WORD -> botNameWordPattern.matcher(lowerText).matches()
        }
    }

    private fun processMessage(text: String, answerOptionals: List<String?>): MessageProcessingResult {
        val answers = answerOptionals
            .filter { it?.isNotBlank() == true }

        if (answers.isEmpty()) { // no answers found
            val noResultResponse = getNoResultResponse(text)
            return MessageProcessingResult.answerNotFound(noResultResponse)
        }

        val combinedAnswers = answers.joinToString("\n\n")
        return MessageProcessingResult.answerFound(combinedAnswers)
    }

    private fun getNoResultResponse(text: String): String? {
        logger.info("Unknown command for the bot: $text")

        return if (wikiBot.settings.replyWhenNoAnswer) { // reply on no answer
            getNoResultAnswer(text)
        }
        else { // no reply on no answer
            null
        }
    }

    // todo: use Kotlin template string in NoAnswerReply instead of {0} and {1} from java
    fun getNoResultAnswer(text: String?): String { // not private for testing
        return MessageFormat.format(
            wikiBot.settings.noAnswerReply,
            botName,
            text
        )
    }
}
