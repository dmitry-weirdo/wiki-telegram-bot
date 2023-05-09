package com.dv.telegram

import com.dv.telegram.BotTestUtils.getUpdate
import com.dv.telegram.BotTestUtils.getWikiBot
import com.dv.telegram.command.GetEnvironment
import com.dv.telegram.command.GetStatistics
import com.dv.telegram.command.Start
import com.dv.telegram.config.ReplyWhenNoAnswer
import com.dv.telegram.data.WikiBotCommandData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class BotGetResponseTest {

    @Test
    @DisplayName("/start command must trigger the bot even from not admin.")
    fun testStartCommandMustTriggerTheBot() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val start = Start()

        val result = wikiBot.processMessage("/start", notBotAdmin, update)

        val expectedResult = MessageProcessingResult.specialCommand(
            start.getResponse("", wikiBot, update),
            false
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Message without bot name must not trigger the bot.")
    fun testMessageWithoutBotNameIsNotForTheBot() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val result = wikiBot.processMessage("message not for the bot", "userName", update)

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot())
    }

    @Test
    @DisplayName("Message without text must not trigger the bot.")
    fun testEmptyMessageIsNotForTheBot() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val result = wikiBot.processMessage("", "userName", update)

        assertThat(result).isEqualTo(MessageProcessingResult.notForTheBot())
    }

    @Test
    @DisplayName("ReplyWhenNoAnswer == true must return \"answer not found\" response.")
    fun testNoResponseWithReplyWhenNoAnswerIsTrue() {
        val wikiBot = getWikiBot {
            it.settings[ReplyWhenNoAnswer.NAME] = true.toString()
            it
        }

        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val text = "$botName, some bad request."
        val result = wikiBot.processMessage(text, notBotAdmin, update)

        val expectedResult = MessageProcessingResult.answerNotFound(
            wikiBot.messageProcessor.getNoResultAnswer(text)
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

        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val result = wikiBot.processMessage("$botName, some bad request.", notBotAdmin, update)

        val expectedResult = MessageProcessingResult.answerNotFound(null)

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by bot admin must return the special command response.")
    fun testGetSpecialCommandResponseCalledByAdmin() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val getEnvironment = GetEnvironment()

        val result = wikiBot.processMessage("$botName ${getEnvironment.defaultCommandName}", botAdmin, update)

        val expectedResult = MessageProcessingResult.specialCommand(
            getEnvironment.getResponse("", wikiBot, update),
            false
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by NOT bot admin must return the non-special command response.")
    fun testGetSpecialCommandResponseCalledByNotAdminWithCommandOverride() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val getEnvironment = GetEnvironment()

        val result = wikiBot.processMessage(
            "$botName ${getEnvironment.defaultCommandName}",
            notBotAdmin,
            update
        )

        val expectedResult = MessageProcessingResult.answerFound(
            "Override of the special command!",
            listOf(ResponseType.COMMAND), // NOT special command
            listOf("/getenvironment") // keyword of a NOT special command
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Special command executed by NOT bot admin must return \"result not found\" response.")
    fun testGetSpecialCommandResponseCalledByNotAdminWithNoOverride() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()
        val notBotAdmin = botAdmin + "_"

        val getStatistics = GetStatistics()

        val text = "$botName ${getStatistics.defaultCommandName}"
        val result = wikiBot.processMessage(text, notBotAdmin, update)

        val expectedResult = MessageProcessingResult.answerNotFound(
            wikiBot.messageProcessor.getNoResultAnswer(text)
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Command must override wiki pages, city chats and country chats.")
    fun testCommandMustOverrideWikiPagesAndChats() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, добрый вечер, дай вики Аугсбург Болгария"
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "Добрый",
            listOf(ResponseType.COMMAND),
            listOf("добрый вечер")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Multiple command matches must return a multiline commands response.")
    fun testCommandMultipleMatches() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, добрый вечер, Слава Україні!"
        val result = wikiBot.processMessage(text, botAdmin, update)

        val command0 = wikiBot.commandTabs[0].tabAnswers.answers[0] as WikiBotCommandData
        val command1 = wikiBot.commandTabs[0].tabAnswers.answers[1] as WikiBotCommandData

        val expectedResult = MessageProcessingResult.answerFound(
            "${command0.getMultiLineAnswer()}\n${command1.getMultiLineAnswer()}",
            listOf(ResponseType.COMMAND),
            listOf("добрый вечер", "слава україні")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Wiki pages single answer.")
    fun testWikiPagesSingleAnswer() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, БезопасностЬ" // must be case-insensitive
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "Правила безопасности — https://uahelp.wiki/safety",
            listOf(ResponseType.WIKI_PAGE),
            listOf("безопасност")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Wiki pages multiple answers.")
    fun testWikiPagesMultipleAnswers() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, БезопасностЬ и вИкИ" // must be case-insensitive
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "▫ Стартовая страница вики — https://uahelp.wiki" +
                    "\n▫ Правила безопасности — https://uahelp.wiki/safety",
            listOf(ResponseType.WIKI_PAGE),
            listOf("вики", "безопасност")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("City chats single answer.")
    fun testCityChatsSingleAnswer() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, АугсБург" // must be case-insensitive
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "Augsburg:" +
                    "\n— https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине" +
                    "\n— https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины" +
                    "\n— https://t.me/Ukr_Augsburg_help — Українці Augsburg",
            listOf(ResponseType.CITY_CHAT),
            listOf("аугсбург")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("City chats multiple answers.")
    fun testCityChatsMultipleAnswers() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, Айзенах и АугсБург" // must be case-insensitive
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "Augsburg:" +
                    "\n— https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине" +
                    "\n— https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины" +
                    "\n— https://t.me/Ukr_Augsburg_help — Українці Augsburg" +
                    "\n\nEisenach:" +
                    "\n— https://t.me/HelpUkraine_Eisenach — Help Ukraine \\uD83C\\uDDFA\\uD83C\\uDDE6 in Eisenach \\uD83C\\uDDE9\\uD83C\\uDDEA",
            listOf(ResponseType.CITY_CHAT),
            listOf("аугсбург", "айзенах")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Country chats single answer.")
    fun testCountryChatsSingleAnswer() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, PortugaliA" // must be case-insensitive
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "Португалия:" +
                    "\n— https://t.me/UAhelpinfo/89 — Общая информация" +
                    "\n— https://t.me/toportugal — Из Украины в Португалию" +
                    "\n— https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи",
            listOf(ResponseType.COUNTRY_CHAT),
            listOf("portugal")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Country chats multiple answers.")
    fun testCountryChatsMultipleAnswers() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, PortugaliA и болгарИя" // must be case-insensitive
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "Болгария:" +
                    "\n— https://t.me/UAhelpinfo/28 — Общая информация" +
                    "\n\nПортугалия:" +
                    "\n— https://t.me/UAhelpinfo/89 — Общая информация" +
                    "\n— https://t.me/toportugal — Из Украины в Португалию" +
                    "\n— https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи",
            listOf(ResponseType.COUNTRY_CHAT),
            listOf("болгария", "portugal")
        )

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("Wiki pages, city chats, country chats must be returned in one list.")
    fun testWikiPagesAndCityChatsAndCountryChats() {
        val wikiBot = getWikiBot()
        val update = getUpdate()

        val botName = wikiBot.botName
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val text = "$botName, PortugaliA, АугсбурГ, вИкИ" // must be case-insensitive
        val result = wikiBot.processMessage(text, botAdmin, update)

        val expectedResult = MessageProcessingResult.answerFound(
            "Стартовая страница вики — https://uahelp.wiki" + // wiki pages
                    "\n\nAugsburg:" + // city chats
                    "\n— https://t.me/NA6R_hilft — Наш Аугсбург помогает Украине" +
                    "\n— https://t.me/augsburgbegi — Аугсбург. Вопросы беженца из Украины" +
                    "\n— https://t.me/Ukr_Augsburg_help — Українці Augsburg" +
                    "\n\nПортугалия:" + // country chats
                    "\n— https://t.me/UAhelpinfo/89 — Общая информация" +
                    "\n— https://t.me/toportugal — Из Украины в Португалию" +
                    "\n— https://t.me/+j3_sMgK6QG8yMmVi — Единый чат по помощи",
            listOf(ResponseType.WIKI_PAGE, ResponseType.CITY_CHAT, ResponseType.COUNTRY_CHAT),
            listOf("вики", "аугсбург", "portugal")
        )

        assertThat(result).isEqualTo(expectedResult)
    }
}
