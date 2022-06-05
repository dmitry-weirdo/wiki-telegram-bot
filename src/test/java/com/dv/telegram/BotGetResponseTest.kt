package com.dv.telegram

import com.dv.telegram.BotTestUtils.getWikiBot
import com.dv.telegram.command.GetEnvironment
import com.dv.telegram.command.GetStatistics
import com.dv.telegram.command.Start
import com.dv.telegram.config.ReplyWhenNoAnswer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

internal class BotGetResponseTest {

    @Test
    @DisplayName("/start command must trigger the bot even from not admin.")
    fun testStartCommandMustTriggerTheBot() {
        val wikiBot = getWikiBot()

        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val start = Start()

        val result = wikiBot.getResponseText("/start", notBotAdmin)

        val expectedResult = MessageProcessingResult.specialCommand(
            Optional.of(start.getResponse("", wikiBot)),
            false
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Message without bot name must not trigger the bot.")
    fun testMessageWithoutBotNameIsNotForTheBot() {
        val wikiBot = getWikiBot()

        val result = wikiBot.getResponseText("message not for the bot", "userName")

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot())
    }

    @Test
    @DisplayName("Message without text must not trigger the bot.")
    fun testEmptyMessageIsNotForTheBot() {
        val wikiBot = getWikiBot()

        val result = wikiBot.getResponseText("", "userName")

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot())
    }

    @Test
    @DisplayName("ReplyWhenNoAnswer == true must return \"answer not found\" response.")
    fun testNoResponseWithReplyWhenNoAnswerIsTrue() {
        val wikiBot = getWikiBot {
            it.settings[ReplyWhenNoAnswer.NAME] = true.toString()
            it
        }

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val text = "$botName, some bad request."
        val result = wikiBot.getResponseText(text, notBotAdmin)

        val expectedResult = MessageProcessingResult.answerNotFound(
            Optional.of(wikiBot.getNoResultAnswer(text))
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("ReplyWhenNoAnswer == false must turn off the \"answer not found\" response.")
    fun testNoResponseWithReplyWhenNoAnswerIsFalse() {
        val wikiBot = getWikiBot {
            it.settings[ReplyWhenNoAnswer.NAME] = false.toString()
            it
        }

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val result = wikiBot.getResponseText("$botName, some bad request.", notBotAdmin)

        val expectedResult = MessageProcessingResult.answerNotFound(
            Optional.empty()
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by bot admin must return the special command response.")
    fun testGetSpecialCommandResponseCalledByAdmin() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val getEnvironment = GetEnvironment()

        val result = wikiBot.getResponseText("$botName ${getEnvironment.defaultCommandName}", botAdmin)

        val expectedResult = MessageProcessingResult.specialCommand(
            Optional.of(getEnvironment.getResponse("", wikiBot)),
            false
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by NOT bot admin must return the non-special command response.")
    fun testGetSpecialCommandResponseCalledByNotAdminWithCommandOverride() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val getEnvironment = GetEnvironment()

        val result = wikiBot.getResponseText("$botName ${getEnvironment.defaultCommandName}", notBotAdmin)

        val expectedResult = MessageProcessingResult.answerFound("Override of the special command!")

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by NOT bot admin must return \"result not found\" response.")
    fun testGetSpecialCommandResponseCalledByNotAdminWithNoOverride() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val getStatistics = GetStatistics()

        val text = "$botName ${getStatistics.defaultCommandName}"
        val result = wikiBot.getResponseText(text, notBotAdmin)

        val expectedResult = MessageProcessingResult.answerNotFound(
            Optional.of(wikiBot.getNoResultAnswer(text))
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Command must override wiki pages, city chats and country chats.")
    fun testCommandMustOverrideWikiPagesAndChats() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, добрый вечер, дай вики Аугсбург Болгария"
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound("Добрый")

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Multiple command matches must return a multiline commands response.")
    fun testCommandMultipleMatches() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, добрый вечер, Слава Україні!"
        val result = wikiBot.getResponseText(text, botAdmin)

        val command0 = wikiBot.commands[0]
        val command1 = wikiBot.commands[1]

        val expectedResult = MessageProcessingResult.answerFound(
            "${command0.getMultiLineAnswer()}\n${command1.getMultiLineAnswer()}"
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Wiki pages single answer.")
    fun testWikiPagesSingleAnswer() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, БезопасностЬ" // must be case-insensitive
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound(
            "Правила безопасности — https://uahelp.wiki/safety"
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Wiki pages multiple answers.")
    fun testWikiPagesMultipleAnswers() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, БезопасностЬ и вИкИ" // must be case-insensitive
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound(
            "— Стартовая страница вики — https://uahelp.wiki" +
                    "\n— Правила безопасности — https://uahelp.wiki/safety"
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("City chats single answer.")
    fun testCityChatsSingleAnswer() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, АугсБург" // must be case-insensitive
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound(
            "Augsburg чаты:" +
                    "\n— https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине" +
                    "\n— https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины" +
                    "\n— https://t.me/Ukr_Augsburg_help — Українці Augsburg"
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("City chats multiple answers.")
    fun testCityChatsMultipleAnswers() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, Айзенах и АугсБург" // must be case-insensitive
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound(
            "Augsburg чаты:" +
                    "\n— https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине" +
                    "\n— https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины" +
                    "\n— https://t.me/Ukr_Augsburg_help — Українці Augsburg" +
                    "\n\nEisenach чаты:" +
                    "\n— https://t.me/HelpUkraine_Eisenach — Help Ukraine \\uD83C\\uDDFA\\uD83C\\uDDE6 in Eisenach \\uD83C\\uDDE9\\uD83C\\uDDEA"
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Country chats single answer.")
    fun testCountryChatsSingleAnswer() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, PortugaliA" // must be case-insensitive
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound(
            "Португалия чаты:" +
                    "\n— https://t.me/UAhelpinfo/89 — Общая информация" +
                    "\n— https://t.me/toportugal — Из Украины в Португалию" +
                    "\n— https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи"
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Country chats multiple answers.")
    fun testCountryChatsMultipleAnswers() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, PortugaliA и болгарИя" // must be case-insensitive
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound(
            "Болгария чаты:" +
                    "\n— https://t.me/UAhelpinfo/28 — Общая информация" +
                    "\n\nПортугалия чаты:" +
                    "\n— https://t.me/UAhelpinfo/89 — Общая информация" +
                    "\n— https://t.me/toportugal — Из Украины в Португалию" +
                    "\n— https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи"
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Wiki pages, city chats, country chats must be returned in one list.")
    fun testWikiPagesAndCityChatsAndCountryChats() {
        val wikiBot = getWikiBot()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, PortugaliA, АугсбурГ, вИкИ" // must be case-insensitive
        val result = wikiBot.getResponseText(text, botAdmin)

        val expectedResult = MessageProcessingResult.answerFound(
            "Стартовая страница вики — https://uahelp.wiki" + // wiki pages
                    "\n\nAugsburg чаты:" + // city chats
                    "\n— https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине" +
                    "\n— https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины" +
                    "\n— https://t.me/Ukr_Augsburg_help — Українці Augsburg" +
                    "\n\nПортугалия чаты:" + // country chats
                    "\n— https://t.me/UAhelpinfo/89 — Общая информация" +
                    "\n— https://t.me/toportugal — Из Украины в Португалию" +
                    "\n— https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи"
        )

        assertThat(result).isEqualTo(expectedResult)
    }
}
