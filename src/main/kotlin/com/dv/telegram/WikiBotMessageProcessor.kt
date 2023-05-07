package com.dv.telegram

import com.dv.telegram.config.BotTriggerMode
import com.dv.telegram.data.BotAnswerDataListResponse
import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.meta.api.objects.Update
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

    fun processMessage(text: String, userName: String, update: Update): MessageProcessingResult { // non-private for testing
        if (text.isBlank()) {
            return MessageProcessingResult.notForTheBot()
        }

        val lowerText = text.lowercase()

        if (!messageIsForTheBot(lowerText)) { // only work when bot is mentioned by name
            return MessageProcessingResult.notForTheBot()
        }

        // special commands - NOT configured in the Google Sheet
        val specialCommandResponse = wikiBot.specialCommands.getResponse(text, userName, wikiBot, update)
        if (specialCommandResponse.hasResponse()) { // special command received -> return response for the special command
            return MessageProcessingResult.specialCommand(
                specialCommandResponse.response,
                specialCommandResponse.useMarkdownInResponse
            )
        }

        // normal commands - configured in the Google Sheet
        for (commandTab in wikiBot.commandTabs) { // iterate in order of priority
            val commandTabAnswer = commandTab.tabAnswers.getResponse(lowerText)

            if (commandTabAnswer.matchFound) { // matching command found -> only handle the command
                return MessageProcessingResult.answerFoundAsCommand(commandTabAnswer)
            }
        }

        // data answers - configured in the Google Sheet
        val dataTabResponses = wikiBot
            .dataTabs
            .map { it.tabAnswers.getResponse(lowerText) }

        return processMessage(text, dataTabResponses)
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

    private fun processMessage(
        text: String,
        responses: List<BotAnswerDataListResponse>
    ): MessageProcessingResult {
        val responseTypes = getResponseTypes(responses)

        val answers = responses
            .filter { it.matchFound }
            .map { it.responseText }
            .filter { it?.isNotBlank() == true }

        if (answers.isEmpty()) { // no answers found
            val noResultResponse = getNoResultResponse(text)
            return MessageProcessingResult.answerNotFound(noResultResponse)
        }

        val combinedAnswers = answers.joinToString("\n\n")

        val matchedKeywordsAll: List<String> = responses
            .map { it.matchedKeywords }
            .flatten()

        // some lists may be empty - filter our the empty values. Super-safe filter out the blank responses.
        val matchedKeywords = matchedKeywordsAll
            .filter { it.isNotBlank() }
            .distinct() // if the word is triggered in multiple tabs (eg "berlin" in chats and wiki pages) - do not count it twice

        return MessageProcessingResult.answerFound(combinedAnswers, responseTypes, matchedKeywords)
    }

    private fun getResponseTypes(responses: List<BotAnswerDataListResponse>) = responses
        .filter { it.matchFound }
        .map { it.responseType }
        .distinct() // in case of different tabs with the same response types, do not add the same response type multiple times

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
