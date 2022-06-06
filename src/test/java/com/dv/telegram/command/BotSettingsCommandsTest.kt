package com.dv.telegram.command

import com.dv.telegram.BotTestUtils
import com.dv.telegram.MessageProcessingResult
import com.dv.telegram.config.*
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

        val listSettings = ListSettings()
        val setSetting = SetSetting()
        val getSetting = GetSetting()

        // execute /listSettings
        val listSettingsResult1 = wikiBot.processMessage("$botName ${listSettings.defaultCommandName}", botAdmin)

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
        val getSettingRequest1 = wikiBot.processMessage("$botName ${getSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME}", botAdmin)

        val expectedGetSettingRequest1 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}*" +
                "\ntrue",
            true
        )

        assertThat(getSettingRequest1).isEqualTo(expectedGetSettingRequest1)

        // try an "answer not found" request
        val answerNotFoundRequest1Text = "$botName bad request."
        val answerNotFoundResult1 = wikiBot.processMessage(answerNotFoundRequest1Text, botAdmin)

        val expectedAnswerNotFoundResult1 = MessageProcessingResult.answerNotFound(
            "${wikiBot.botName} ничего не знает про ваш запрос «$answerNotFoundRequest1Text» :("
        )

        assertThat(answerNotFoundResult1).isEqualTo(expectedAnswerNotFoundResult1)

        // set ReplyWhenNoAnswer to false
        val setSettingRequest1 = wikiBot.processMessage("$botName ${setSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME} false", botAdmin)

        val expectedSetSettingRequest1 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}* установлена в значение" +
                "\nfalse",
            true
        )

        assertThat(setSettingRequest1).isEqualTo(expectedSetSettingRequest1)

        // check /listSettings update
        val listSettingsResult2 = wikiBot.processMessage("$botName ${listSettings.defaultCommandName}", botAdmin)

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
        val getSettingRequest2 = wikiBot.processMessage("$botName ${getSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME}", botAdmin)

        val expectedGetSettingRequest2 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}*" +
                "\nfalse",
            true
        )

        assertThat(getSettingRequest2).isEqualTo(expectedGetSettingRequest2)

        // try an "answer not found" request -> no answer expected
        val answerNotFoundRequest2Text = "$botName bad request."
        val answerNotFoundResult2 = wikiBot.processMessage(answerNotFoundRequest2Text, botAdmin)

        val expectedAnswerNotFoundResult2 = MessageProcessingResult.answerNotFound(null)

        assertThat(answerNotFoundResult2).isEqualTo(expectedAnswerNotFoundResult2)

        // set ReplyWhenNoAnswer to true
        val setSettingRequest2 = wikiBot.processMessage("$botName ${setSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME} true", botAdmin)

        val expectedSetSettingRequest2 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}* установлена в значение" +
                "\ntrue",
            true
        )

        assertThat(setSettingRequest2).isEqualTo(expectedSetSettingRequest2)

        // set NoAnswerReply to new value
        val noAnswerReplyNewValue = "{0} has no idea about this!"

        val setSettingRequest3 = wikiBot.processMessage("$botName ${setSetting.defaultCommandName} ${NoAnswerReply.NAME} $noAnswerReplyNewValue", botAdmin)

        val expectedSetSettingRequest3 = MessageProcessingResult.specialCommand(
            "*${NoAnswerReply.NAME}* установлена в значение" +
                "\n$noAnswerReplyNewValue",
            true
        )

        assertThat(setSettingRequest3).isEqualTo(expectedSetSettingRequest3)

        // check /listSettings update
        val listSettingsResult3 = wikiBot.processMessage("$botName ${listSettings.defaultCommandName}", botAdmin)

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
        val getSettingRequest3 = wikiBot.processMessage("$botName ${getSetting.defaultCommandName} ${ReplyWhenNoAnswer.NAME}", botAdmin)

        val expectedGetSettingRequest3 = MessageProcessingResult.specialCommand(
            "*${ReplyWhenNoAnswer.NAME}*" +
                "\ntrue",
            true
        )

        assertThat(getSettingRequest3).isEqualTo(expectedGetSettingRequest3)

        // check /getSetting NoAnswerReply update
        val getSettingRequest4 = wikiBot.processMessage("$botName ${getSetting.defaultCommandName} ${NoAnswerReply.NAME}", botAdmin)

        val expectedGetSettingRequest4 = MessageProcessingResult.specialCommand(
            "*${NoAnswerReply.NAME}*" +
                "\n$noAnswerReplyNewValue",
            true
        )

        assertThat(getSettingRequest4).isEqualTo(expectedGetSettingRequest4)

        // try an "answer not found" request -> "answer not found" expected with new format
        val answerNotFoundRequest3Text = "$botName bad request."
        val answerNotFoundResult3 = wikiBot.processMessage(answerNotFoundRequest3Text, botAdmin)

        val expectedAnswerNotFoundResult3 = MessageProcessingResult.answerNotFound(
            "${wikiBot.botName} has no idea about this!"
        )

        assertThat(answerNotFoundResult3).isEqualTo(expectedAnswerNotFoundResult3)
    }

    @Test
    @DisplayName("Test /helpSetting command.")
    fun testHelpSetting() {
        // todo: implement method
    }

    // todo: /helpSetting for unknown setting
    // todo: /setSetting for unknown setting
    // todo: /getSetting for unknown setting
}
