package com.dv.telegram.command

import com.dv.telegram.BotTestUtils
import com.dv.telegram.MessageProcessingResult
import com.dv.telegram.config.BotTriggerMode
import com.dv.telegram.config.DeleteBotCallMessageOnMessageReply
import com.dv.telegram.config.NoAnswerReply
import com.dv.telegram.config.ReplyWhenNoAnswer
import com.dv.telegram.config.StartMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class BotSettingsCommandsTest {

    @Test
    @DisplayName("Test bot commands related to bot settings (successful path: settings list, update and work).")
    fun testBotSettingsCommands() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val update = BotTestUtils.getUpdate()

        val listSettings = ListSettings()
        val setSetting = SetSetting()
        val getSetting = GetSetting()

        // execute /listSettings
        val listSettingsResult1 = wikiBot.processMessage(
            "$botName ${listSettings.defaultCommandName}",
            botAdmin,
            update
        )

        // initial values from test-config.json
        val expectedListSettingsResult1 = MessageProcessingResult.specialCommand(
            "— *${StartMessage.NAME}*:" +
                "\nПривет! Меня зовут {0}.\n\nЗадавайте мне вопросы, обращаясь ко мне по имени.\nНапример:\n— {0}, как найти работу?\n— {0} где курсы немецкого?\n— {0} FAQ?\n\nЯ буду стараться ответить на все вопросы, ответам на которые меня научили!" +
                "\n\n— *${DeleteBotCallMessageOnMessageReply.NAME}*:" +
                "\ntrue" +
                "\n\n— *${BotTriggerMode.NAME}*:" +
                "\nFULL\\_WORD" + // _ must be escaped to not break the Markdown
                "\n\n— *${ReplyWhenNoAnswer.NAME}*:" +
                "\ntrue" +
                "\n\n— *${NoAnswerReply.NAME}*:" +
                "\n{0} ничего не знает про ваш запрос «{1}» :(",
            true
        )

        assertThat(listSettingsResult1).isEqualTo(expectedListSettingsResult1)

        // execute /getSetting ReplyWhenNoAnswer
        val getSettingRequest1 = wikiBot.processMessage(
            "$botName ${getSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME}",
            botAdmin,
            update
        )

        val expectedGetSettingRequest1 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}*" +
                "\ntrue",
            true
        )

        assertThat(getSettingRequest1).isEqualTo(expectedGetSettingRequest1)

        // try an "answer not found" request
        val answerNotFoundRequest1Text = "$botName bad request."
        val answerNotFoundResult1 = wikiBot.processMessage(answerNotFoundRequest1Text, botAdmin, update)

        val expectedAnswerNotFoundResult1 = MessageProcessingResult.answerNotFound(
            "${wikiBot.botName} ничего не знает про ваш запрос «$answerNotFoundRequest1Text» :("
        )

        assertThat(answerNotFoundResult1).isEqualTo(expectedAnswerNotFoundResult1)

        // set ReplyWhenNoAnswer to false
        val setSettingRequest1 = wikiBot.processMessage(
            "$botName ${setSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME} false",
            botAdmin,
            update
        )

        val expectedSetSettingRequest1 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}* установлена в значение" +
                "\nfalse",
            true
        )

        assertThat(setSettingRequest1).isEqualTo(expectedSetSettingRequest1)

        // check /listSettings update
        val listSettingsResult2 = wikiBot.processMessage(
            "$botName ${listSettings.defaultCommandName}",
            botAdmin,
            update
        )

        val expectedListSettingsResult2 = MessageProcessingResult.specialCommand(
            "— *${StartMessage.NAME}*:" +
                "\nПривет! Меня зовут {0}.\n\nЗадавайте мне вопросы, обращаясь ко мне по имени.\nНапример:\n— {0}, как найти работу?\n— {0} где курсы немецкого?\n— {0} FAQ?\n\nЯ буду стараться ответить на все вопросы, ответам на которые меня научили!" +
                "\n\n— *${DeleteBotCallMessageOnMessageReply.NAME}*:" +
                "\ntrue" +
                "\n\n— *${BotTriggerMode.NAME}*:" +
                "\nFULL\\_WORD" + // _ must be escaped to not break the Markdown
                "\n\n— *${ReplyWhenNoAnswer.NAME}*:" +
                "\nfalse" + // must change
                "\n\n— *${NoAnswerReply.NAME}*:" +
                "\n{0} ничего не знает про ваш запрос «{1}» :(",
            true
        )

        assertThat(listSettingsResult2).isEqualTo(expectedListSettingsResult2)

        // check /getSetting ReplyWhenNoAnswer update
        val getSettingRequest2 = wikiBot.processMessage(
            "$botName ${getSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME}",
            botAdmin,
            update
        )

        val expectedGetSettingRequest2 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}*" +
                "\nfalse",
            true
        )

        assertThat(getSettingRequest2).isEqualTo(expectedGetSettingRequest2)

        // try an "answer not found" request -> no answer expected
        val answerNotFoundRequest2Text = "$botName bad request."
        val answerNotFoundResult2 = wikiBot.processMessage(answerNotFoundRequest2Text, botAdmin, update)

        val expectedAnswerNotFoundResult2 = MessageProcessingResult.answerNotFound(null)

        assertThat(answerNotFoundResult2).isEqualTo(expectedAnswerNotFoundResult2)

        // set ReplyWhenNoAnswer to true
        val setSettingRequest2 = wikiBot.processMessage(
            "$botName ${setSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME} true",
            botAdmin,
            update
        )

        val expectedSetSettingRequest2 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}* установлена в значение" +
                "\ntrue",
            true
        )

        assertThat(setSettingRequest2).isEqualTo(expectedSetSettingRequest2)

        // set NoAnswerReply to new value
        val noAnswerReplyNewValue = "{0} has no idea about this!"

        val setSettingRequest3 = wikiBot.processMessage(
            "$botName ${setSetting.defaultCommandName} ${NoAnswerReply.NAME} $noAnswerReplyNewValue",
            botAdmin,
            update
        )

        val expectedSetSettingRequest3 = MessageProcessingResult.specialCommand(
            "*${NoAnswerReply.NAME}* установлена в значение" +
                "\n$noAnswerReplyNewValue",
            true
        )

        assertThat(setSettingRequest3).isEqualTo(expectedSetSettingRequest3)

        // check /listSettings update
        val listSettingsResult3 = wikiBot.processMessage(
            "$botName ${listSettings.defaultCommandName}",
            botAdmin,
            update
        )

        val expectedListSettingsResult3 = MessageProcessingResult.specialCommand(
            "— *${StartMessage.NAME}*:" +
                "\nПривет! Меня зовут {0}.\n\nЗадавайте мне вопросы, обращаясь ко мне по имени.\nНапример:\n— {0}, как найти работу?\n— {0} где курсы немецкого?\n— {0} FAQ?\n\nЯ буду стараться ответить на все вопросы, ответам на которые меня научили!" +
                "\n\n— *${DeleteBotCallMessageOnMessageReply.NAME}*:" +
                "\ntrue" +
                "\n\n— *${BotTriggerMode.NAME}*:" +
                "\nFULL\\_WORD" + // _ must be escaped to not break the Markdown
                "\n\n— *${ReplyWhenNoAnswer.NAME}*:" +
                "\ntrue" + // must change
                "\n\n— *${NoAnswerReply.NAME}*:" +
                "\n$noAnswerReplyNewValue", // must change
            true
        )

        assertThat(listSettingsResult3).isEqualTo(expectedListSettingsResult3)

        // check /getSetting ReplyWhenNoAnswer update
        val getSettingRequest3 = wikiBot.processMessage(
            "$botName ${getSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME}",
            botAdmin,
            update
        )

        val expectedGetSettingRequest3 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}*" +
                "\ntrue",
            true
        )

        assertThat(getSettingRequest3).isEqualTo(expectedGetSettingRequest3)

        // check /getSetting NoAnswerReply update
        val getSettingRequest4 = wikiBot.processMessage(
            "$botName ${getSetting.defaultCommandName} ${NoAnswerReply.NAME}",
            botAdmin,
            update
        )

        val expectedGetSettingRequest4 = MessageProcessingResult.specialCommand(
            "*${NoAnswerReply.NAME}*" +
                "\n$noAnswerReplyNewValue",
            true
        )

        assertThat(getSettingRequest4).isEqualTo(expectedGetSettingRequest4)

        // try an "answer not found" request -> "answer not found" expected with new format
        val answerNotFoundRequest3Text = "$botName bad request."
        val answerNotFoundResult3 = wikiBot.processMessage(answerNotFoundRequest3Text, botAdmin, update)

        val expectedAnswerNotFoundResult3 = MessageProcessingResult.answerNotFound(
            "${wikiBot.botName} has no idea about this!"
        )

        assertThat(answerNotFoundResult3).isEqualTo(expectedAnswerNotFoundResult3)
    }

    @Test
    @DisplayName("Test /getSetting command: incorrect setting names.")
    fun testGetSettingIncorrectSettingNames() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val update = BotTestUtils.getUpdate()

        val getSetting = GetSetting()

        // no setting name
        val noSettingNameResult = wikiBot.processMessage(
            "$botName ${getSetting.defaultCommandName}",
            botAdmin,
            update
        )

        val expectedNoSettingNameResult = MessageProcessingResult.specialCommand("Неизвестное имя настройки.", true)

        assertThat(noSettingNameResult).isEqualTo(expectedNoSettingNameResult)

        // incorrect setting name
        val incorrectSettingNameResult = wikiBot.processMessage(
            "$botName ${getSetting.defaultCommandName} BadSetting",
            botAdmin,
            update
        )

        val expectedIncorrectSettingNameResult = MessageProcessingResult.specialCommand("Неизвестное имя настройки.", true)

        assertThat(incorrectSettingNameResult).isEqualTo(expectedIncorrectSettingNameResult)
    }

    @Test
    @DisplayName("Test /setSetting command: incorrect setting names.")
    fun testSetSettingIncorrectSettingNames() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val update = BotTestUtils.getUpdate()

        val setSetting = SetSetting()

        // no setting name
        val noSettingNameResult = wikiBot.processMessage(
            "$botName ${setSetting.defaultCommandName}",
            botAdmin,
            update
        )

        val expectedNoSettingNameResult = MessageProcessingResult.specialCommand("Неизвестное имя настройки.", true)

        assertThat(noSettingNameResult).isEqualTo(expectedNoSettingNameResult)

        // incorrect setting name
        val incorrectSettingNameResult = wikiBot.processMessage(
            "$botName ${setSetting.defaultCommandName} BadSetting",
            botAdmin,
            update
        )

        val expectedIncorrectSettingNameResult = MessageProcessingResult.specialCommand("Неизвестное имя настройки.", true)

        assertThat(incorrectSettingNameResult).isEqualTo(expectedIncorrectSettingNameResult)
    }

    @Test
    @DisplayName("Test /setSetting command: incorrect setting value.")
    fun testSetSettingIncorrectSettingValue() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val update = BotTestUtils.getUpdate()

        val listSettings = ListSettings()
        val getSetting = GetSetting()
        val setSetting = SetSetting()

        // incorrect setting value
        val incorrectValue = "BAD-VALUE"
        val incorrectSettingValueResult = wikiBot.processMessage(
            "$botName ${setSetting.defaultCommandName} ${BotTriggerMode.NAME} $incorrectValue",
            botAdmin,
            update
        )

        val expectedIncorrectSettingValueResult = MessageProcessingResult.specialCommand(
            "Ошибка при установке настройки *${BotTriggerMode.NAME}* в значение" +
                "\n$incorrectValue",
            true
        )
        assertThat(incorrectSettingValueResult).isEqualTo(expectedIncorrectSettingValueResult)

        // /getSetting must not change
        val getSettingRequest = wikiBot.processMessage(
            "$botName ${getSetting.defaultCommandName} ${BotTriggerMode.NAME}",
            botAdmin,
            update
        )

        val expectedGetSettingRequest = MessageProcessingResult.specialCommand(
            "*${BotTriggerMode.NAME}*" +
                "\nFULL\\_WORD",
            true
        )

        assertThat(getSettingRequest).isEqualTo(expectedGetSettingRequest)

        // /listSettings must not change
        val listSettingsResult = wikiBot.processMessage(
            "$botName ${listSettings.defaultCommandName}",
            botAdmin,
            update
        )

        // initial values from test-config.json
        val expectedListSettingsResult = MessageProcessingResult.specialCommand(
            "— *${StartMessage.NAME}*:" +
                "\nПривет! Меня зовут {0}.\n\nЗадавайте мне вопросы, обращаясь ко мне по имени.\nНапример:\n— {0}, как найти работу?\n— {0} где курсы немецкого?\n— {0} FAQ?\n\nЯ буду стараться ответить на все вопросы, ответам на которые меня научили!" +
                "\n\n— *${DeleteBotCallMessageOnMessageReply.NAME}*:" +
                "\ntrue" +
                "\n\n— *${BotTriggerMode.NAME}*:" +
                "\nFULL\\_WORD" + // _ must be escaped to not break the Markdown
                "\n\n— *${ReplyWhenNoAnswer.NAME}*:" +
                "\ntrue" +
                "\n\n— *${NoAnswerReply.NAME}*:" +
                "\n{0} ничего не знает про ваш запрос «{1}» :(",
            true
        )

        assertThat(listSettingsResult).isEqualTo(expectedListSettingsResult)
    }

    @Test
    @DisplayName("Test /helpSetting command: correct setting name, successful path.")
    fun testHelpSetting() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val update = BotTestUtils.getUpdate()

        val helpSetting = HelpSetting()

        // execute /helpSetting ReplyWhenNoAnswer
        val helpSettingResult = wikiBot.processMessage(
            "$botName ${helpSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME}",
            botAdmin,
            update
        )

        val expectedHelpSettingResult = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}*"
                + "\n*true* — Если бот не нашёл ответа, он выдаст ответ, определённый настройкой *${NoAnswerReply.NAME}*."
                + "\n\n*false* — Если бот не нашёл ответа, он не будет отвечать.",
            true
        )

        assertThat(helpSettingResult).isEqualTo(expectedHelpSettingResult)
    }

    @Test
    @DisplayName("Test /helpSetting command: incorrect setting names.")
    fun testHelpSettingIncorrectSettingNames() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val update = BotTestUtils.getUpdate()

        val helpSetting = HelpSetting()

        // no setting name
        val noSettingNameResult = wikiBot.processMessage(
            "$botName ${helpSetting.defaultCommandName}",
            botAdmin,
            update
        )

        val expectedNoSettingNameResult = MessageProcessingResult.specialCommand("Неизвестное имя настройки.", true)

        assertThat(noSettingNameResult).isEqualTo(expectedNoSettingNameResult)

        // incorrect setting name
        val incorrectSettingNameResult = wikiBot.processMessage(
            "$botName ${helpSetting.defaultCommandName} BadSetting",
            botAdmin,
            update
        )

        val expectedIncorrectSettingNameResult = MessageProcessingResult.specialCommand("Неизвестное имя настройки.", true)

        assertThat(incorrectSettingNameResult).isEqualTo(expectedIncorrectSettingNameResult)
    }
}
