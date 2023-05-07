package com.dv.telegram

import com.dv.telegram.command.BotSpecialCommands
import com.dv.telegram.config.BotSettings
import com.dv.telegram.data.BotAnswerData
import com.dv.telegram.data.CityChatData
import com.dv.telegram.data.CityChatsDataList
import com.dv.telegram.data.CountryChatData
import com.dv.telegram.data.CountryChatsDataList
import com.dv.telegram.data.WikiBotCommandData
import com.dv.telegram.data.WikiBotCommandsDataList
import com.dv.telegram.data.WikiPageData
import com.dv.telegram.data.WikiPagesDataList
import com.dv.telegram.statistics.BotStatistics
import com.dv.telegram.tabs.BotAnswerTabData
import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetMe
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class WikiBot(
    val context: WikiBotsContext,
    private val config: WikiBotConfig,
    wikiPagesData: List<WikiPageData>,
    cityChatsData: List<CityChatData>,
    countryChatsData: List<CountryChatData>,
    commandsData: List<WikiBotCommandData>
) : TelegramLongPollingBot(), Logging {
    var commandTabs: List<BotAnswerTabData<BotAnswerData>> // can be reloaded from Google Sheet
        private set

    var dataTabs: List<BotAnswerTabData<BotAnswerData>> // can be reloaded from Google Sheet
        private set

    var pages: WikiPagesDataList // can be reloaded from Google Sheet
        private set

    var cityChats: CityChatsDataList // can be reloaded from Google Sheet
        private set

    var countryChats: CountryChatsDataList // can be reloaded from Google Sheet
        private set

    var commands: WikiBotCommandsDataList // can be reloaded from Google Sheet
        private set

    val botName: String

    val environmentName: String

    val specialCommands: BotSpecialCommands

    val settings: BotSettings // settings cache in memory

    val statistics: BotStatistics

    val messageProcessor: WikiBotMessageProcessor

    var lastMessageLog: MessageProcessingLog? = null

    var telegramName: String? = null

    constructor(
        context: WikiBotsContext,
        config: WikiBotConfig,
        botData: GoogleSheetBotData
    ) : this(
        context,
        config,
        botData.pages,
        botData.cityChats,
        botData.countryChats,
        botData.commands
    )

    init {
        context.addBot(this)

        // todo: parse from the input parameters
        commandTabs = listOf()
        dataTabs = listOf()

        pages = WikiPagesDataList(wikiPagesData)
        cityChats = CityChatsDataList(cityChatsData)
        countryChats = CountryChatsDataList(countryChatsData)
        commands = WikiBotCommandsDataList(commandsData)

        botName = config.botName

        environmentName = config.environmentName

        specialCommands = BotSpecialCommands.create(config)
        settings = BotSettings.create(config)

        statistics = BotStatistics()

        messageProcessor = WikiBotMessageProcessor(this)
    }

    // additional getters
    val commandSheetName: String
        get() = config.commandsSheetName

    val notionToken: String
        get() = config.notionToken

    val notionCityPageId: String
        get() = config.cityChatsPageId

    val notionCityChatsToggleHeading1Text: String
        get() = config.cityChatsToggleHeading1Text

    // functions overridden from Telegram API (TelegramLongPollingBot and its parent hierarchy)
    override fun getBotToken(): String {
        return config.botToken
    }

    override fun getBotUsername(): String {
        return "dv_wiki_bot" // todo: read from config if needed. Seems to be overridden by what is set by "/setname" in the @BotFather
    }

    override fun onUpdateReceived(update: Update) {
        // todo: env option to disable the bot globally!
        val updateMessage = update.message
        if (!update.hasMessage() || !updateMessage.hasText()) {
            return
        }

        val text = updateMessage.text
        val userName = updateMessage.from.userName ?: "" // userName is nullable, user can have no username in Telegram

        val processingResult = processMessage(text, userName, update)

        if (!processingResult.messageIsForTheBot) { // message is not for the bot -> do nothing
            return
        }

        // if the message is a reply, is for the bot and ReplyWhenNoAnswer is false, we still have to delete it
        val replyToMessage = updateMessage.replyToMessage
        val updateMessageIsReply = (replyToMessage != null)

        val deleteBotCallMessage = settings.deleteBotCallMessageOnMessageReply && updateMessageIsReply
        if (deleteBotCallMessage) {
            deleteBotCallMessage(updateMessage)
        }

        if (processingResult.hasNoResponse()) { // no response -> nothing to return
            return
        }

        // command is for the bot and has the response -> send the response message
        try {
            val sendMessages = createSendMessage(updateMessage, replyToMessage, deleteBotCallMessage, processingResult)

            for (sendMessage in sendMessages) { // messages are split into 4096 characters
                execute(sendMessage) // Call method to send the message
            }
        }
        catch (e: TelegramApiException) {
            logger.error("TelegramApiException occurred on sending the bot response message", e) // todo: add which message has failed the bot
            throw RuntimeException(e)
        }
    }

    private fun createSendMessage(
        updateMessage: Message,
        replyToMessage: Message?,
        deleteBotCallMessage: Boolean,
        processingResult: MessageProcessingResult
    ): List<SendMessage> {
        val chatId = updateMessage.chatId.toString()
        val responseText = processingResult.getResponseOrFail()

        // todo: probably some more intellectual split like on new lines near the limits
        val responseTexts = responseText.chunked(MAX_CHARACTERS_IN_MESSAGE)

        val messages = mutableListOf<SendMessage>()

        for (messageText in responseTexts) {
            val replyToMessageId =
                if (deleteBotCallMessage) { // reply to the original message
                    replyToMessage!!.messageId
                }
                else { // reply to the "call bot" message
                    updateMessage.messageId
                }

            val sendMessage = SendMessage()
            sendMessage.chatId = chatId
            sendMessage.replyToMessageId = replyToMessageId
            sendMessage.text = messageText
            sendMessage.disableWebPagePreview()

            // todo: markdown can be broken if it is split into different message parts. Probably turn it off if responseTexts.size > 1
            if (processingResult.useMarkdown) {
                sendMessage.parseMode = "Markdown"
            }

            messages.add(sendMessage)
        }

        return messages
    }

    private fun deleteBotCallMessage(updateMessage: Message) {
        val deleteMessage = DeleteMessage(
            updateMessage.chatId.toString(),
            updateMessage.messageId
        )

        try {
            execute(deleteMessage)
        }
        catch (e: TelegramApiException) {
            logger.error("Failed to delete message", e)
        }
    }

    fun processMessage(text: String, userName: String, update: Update): MessageProcessingResult { // non-private for testing
        val result = messageProcessor.processMessage(text, userName, update)

        statistics.update(text, result) // moved into this method to be updated in the test

        if (result.messageIsForTheBot) {
            lastMessageLog = MessageProcessingLog.create(this, update, result)
        }

        return result
    }

    fun getTelegramUserName(): String {
        if (telegramName != null) {
            return telegramName!!
        }

        return try {
            val getMe = GetMe()
            val getMeResult = execute(getMe)

            telegramName = getMeResult.userName

            telegramName!!
        }
        catch (e: Exception) {
            logger.error("Error when getting Telegram user name for bot $botName", e)
            "unknown"
        }
    }

    fun reloadBotDataFromGoogleSheet(): Boolean {
        return try {
            val botData = loadBotDataFromGoogleSheet()
            pages = WikiPagesDataList(botData)
            cityChats = CityChatsDataList(botData)
            countryChats = CountryChatsDataList(botData)
            commands = WikiBotCommandsDataList(botData)

            true
        }
        catch (e: Exception) {
            false
        }
    }

    fun reloadBotDataFromGoogleSheetTabs(): Boolean {
        return try {
            val botData = loadBotDataFromGoogleSheetTabs()

            commandTabs = botData.commandTabs
                .map { BotAnswerTabData.fromTabData(it) }

            dataTabs = botData.dataTabs
                .map { BotAnswerTabData.fromTabData(it) }

            true
        }
        catch (e: Exception) {
            false
        }
    }

    fun loadBotDataFromGoogleSheet(): GoogleSheetBotData { // does NOT reload the bot data itself
        return GoogleSheetLoader.readGoogleSheet(config)
    }

    fun loadBotDataFromGoogleSheetTabs(): GoogleSheetBotTabsData { // does NOT reload the bot data itself
        return GoogleSheetLoader.readGoogleSheetTabs(config)
    }

    companion object {
        private const val MAX_CHARACTERS_IN_MESSAGE = 4096
    }
}
