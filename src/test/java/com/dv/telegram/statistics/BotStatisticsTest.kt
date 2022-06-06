package com.dv.telegram.statistics

import com.dv.telegram.BotTestUtils
import com.dv.telegram.MessageProcessingResult
import com.dv.telegram.command.GetEnvironment
import com.dv.telegram.command.GetFailedRequests
import com.dv.telegram.command.GetStatistics
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class BotStatisticsTest {

    @Test
    @DisplayName("Test bot statistics")
    fun testBotStatistics() {
        val wikiBot = BotTestUtils.getWikiBot()
        val botName = wikiBot.botName.uppercase() // match must be case-insensitive
        val botAdmin = wikiBot.specialCommands.botAdmins.iterator().next()

        val statistics = wikiBot.statistics
        val startTime = statistics.startTime
        assertThat(startTime).isBefore(ZonedDateTime.now())

        // todo: if possible with Kotlin, generate Assert for BotStatistics and validate with nicer code
        // start - starts is empty
        assertThat(statistics.startTime).isEqualTo(startTime)
        assertThat(statistics.successfulRequestsCount).isZero
        assertThat(statistics.successfulRequestsCountWithPercentage).isEqualTo("0 (0.00 %)")
        assertThat(statistics.failedRequestsCount).isZero
        assertThat(statistics.failedRequestsCountWithPercentage).isEqualTo("0 (0.00 %)")
        assertThat(statistics.failedRequests).isEmpty()
        assertThat(statistics.totalCalls).isZero
        assertThat(statistics.totalCallsWithPercentage).isEqualTo("0 (100.00 %)")
        assertThat(statistics.specialCommandsCount).isZero
        assertThat(statistics.totalCallsWithSpecialCommands).isZero

        // message not for the bot must not affect stats
        val notForTheBotResult = wikiBot.processMessage("Not for the bot", botAdmin)
        assertThat(notForTheBotResult).isEqualTo(MessageProcessingResult.notForTheBot())

        assertThat(statistics.startTime).isEqualTo(startTime)
        assertThat(statistics.successfulRequestsCount).isZero
        assertThat(statistics.successfulRequestsCountWithPercentage).isEqualTo("0 (0.00 %)")
        assertThat(statistics.failedRequestsCount).isZero
        assertThat(statistics.failedRequestsCountWithPercentage).isEqualTo("0 (0.00 %)")
        assertThat(statistics.failedRequests).isEmpty()
        assertThat(statistics.totalCalls).isZero
        assertThat(statistics.totalCallsWithPercentage).isEqualTo("0 (100.00 %)")
        assertThat(statistics.specialCommandsCount).isZero
        assertThat(statistics.totalCallsWithSpecialCommands).isZero

        // execute special command
        val getEnvironment = GetEnvironment()
        val getEnvironmentResult = wikiBot.processMessage("$botName ${getEnvironment.defaultCommandName}", botAdmin)

        val expectedGetEnvironmentResult = MessageProcessingResult.specialCommand(
            getEnvironment.getResponse("", wikiBot),
            false
        )

        assertThat(getEnvironmentResult).isEqualTo(expectedGetEnvironmentResult)

        assertThat(statistics.startTime).isEqualTo(startTime)
        assertThat(statistics.successfulRequestsCount).isZero
        assertThat(statistics.successfulRequestsCountWithPercentage).isEqualTo("0 (0.00 %)")
        assertThat(statistics.failedRequestsCount).isZero
        assertThat(statistics.failedRequestsCountWithPercentage).isEqualTo("0 (0.00 %)")
        assertThat(statistics.failedRequests).isEmpty()
        assertThat(statistics.totalCalls).isZero
        assertThat(statistics.totalCallsWithPercentage).isEqualTo("0 (100.00 %)")
        assertThat(statistics.specialCommandsCount).isEqualTo(1) // updated
        assertThat(statistics.totalCallsWithSpecialCommands).isEqualTo(1) // updated

        // execute successful request
        val successfulRequestResult = wikiBot.processMessage("$botName Айзенах", botAdmin)

        val expectedSuccessfulRequestResult = MessageProcessingResult.answerFound(
            "Eisenach чаты:" +
                "\n— https://t.me/HelpUkraine_Eisenach — Help Ukraine \\uD83C\\uDDFA\\uD83C\\uDDE6 in Eisenach \\uD83C\\uDDE9\\uD83C\\uDDEA"
        )

        assertThat(successfulRequestResult).isEqualTo(expectedSuccessfulRequestResult)

        assertThat(statistics.startTime).isEqualTo(startTime)
        assertThat(statistics.successfulRequestsCount).isEqualTo(1) // updated
        assertThat(statistics.successfulRequestsCountWithPercentage).isEqualTo("1 (100.00 %)") // updated
        assertThat(statistics.failedRequestsCount).isZero
        assertThat(statistics.failedRequestsCountWithPercentage).isEqualTo("0 (0.00 %)")
        assertThat(statistics.failedRequests).isEmpty()
        assertThat(statistics.totalCalls).isEqualTo(1) // updated
        assertThat(statistics.totalCallsWithPercentage).isEqualTo("1 (100.00 %)") // updated
        assertThat(statistics.specialCommandsCount).isEqualTo(1)
        assertThat(statistics.totalCallsWithSpecialCommands).isEqualTo(2) // updated

        // execute failed request
        val failedRequestText = "$botName bad request"
        val failedRequestResult = wikiBot.processMessage(failedRequestText, botAdmin)

        val expectedFailedRequestResult = MessageProcessingResult.answerNotFound(
            wikiBot.messageProcessor.getNoResultAnswer(failedRequestText)
        )

        assertThat(failedRequestResult).isEqualTo(expectedFailedRequestResult)

        assertThat(statistics.startTime).isEqualTo(startTime)
        assertThat(statistics.successfulRequestsCount).isEqualTo(1)
        assertThat(statistics.successfulRequestsCountWithPercentage).isEqualTo("1 (50.00 %)") // updated
        assertThat(statistics.failedRequestsCount).isEqualTo(1) // updated
        assertThat(statistics.failedRequestsCountWithPercentage).isEqualTo("1 (50.00 %)") // updated
        assertThat(statistics.failedRequests).containsExactly(failedRequestText) // updated
        assertThat(statistics.totalCalls).isEqualTo(2) // updated
        assertThat(statistics.totalCallsWithPercentage).isEqualTo("2 (100.00 %)") // updated
        assertThat(statistics.specialCommandsCount).isEqualTo(1)
        assertThat(statistics.totalCallsWithSpecialCommands).isEqualTo(3) // updated

        // execute /getFailedRequests
        val getFailedRequests = GetFailedRequests()

        val getFailedRequestsResult = wikiBot.processMessage("$botName ${getFailedRequests.defaultCommandName}", botAdmin)

        val expectedGetFailedRequestsResult = MessageProcessingResult.specialCommand(
            "Разных неудачных запросов: 1" +
                "\n— $failedRequestText",
            false
        )

        assertThat(getFailedRequestsResult).isEqualTo(expectedGetFailedRequestsResult)

        // /getFailedRequests is also a special command
        assertThat(statistics.startTime).isEqualTo(startTime)
        assertThat(statistics.successfulRequestsCount).isEqualTo(1)
        assertThat(statistics.successfulRequestsCountWithPercentage).isEqualTo("1 (50.00 %)")
        assertThat(statistics.failedRequestsCount).isEqualTo(1)
        assertThat(statistics.failedRequestsCountWithPercentage).isEqualTo("1 (50.00 %)")
        assertThat(statistics.failedRequests).containsExactly(failedRequestText)
        assertThat(statistics.totalCalls).isEqualTo(2)
        assertThat(statistics.totalCallsWithPercentage).isEqualTo("2 (100.00 %)")
        assertThat(statistics.specialCommandsCount).isEqualTo(2)  // updated
        assertThat(statistics.totalCallsWithSpecialCommands).isEqualTo(4) // updated

        // execute /getStats
        val getStatistics = GetStatistics()

        val getStatisticsResult = wikiBot.processMessage("$botName ${getStatistics.defaultCommandName}", botAdmin)

        val startTimeString = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss").format(startTime)

        val expectedGetStatisticsResult = MessageProcessingResult.specialCommand(
            "— Время старта бота: $startTimeString" +
                "\n— Успешных запросов: 1 (50.00 %)" +
                "\n— Неуспешных запросов: 1 (50.00 %)" +
                "\n— Всего запросов: 2 (100.00 %)" +
                "\n— Вызовов специальных команд: 2" + // stats updated after the /getStatistics execution
                "\n— Всего запросов (вместе со специальными командами): 4", // stats updated after the /getStatistics execution
            false
        )

        assertThat(getStatisticsResult).isEqualTo(expectedGetStatisticsResult)

        // /getStats is also a special command
        assertThat(statistics.startTime).isEqualTo(startTime)
        assertThat(statistics.successfulRequestsCount).isEqualTo(1)
        assertThat(statistics.successfulRequestsCountWithPercentage).isEqualTo("1 (50.00 %)")
        assertThat(statistics.failedRequestsCount).isEqualTo(1)
        assertThat(statistics.failedRequestsCountWithPercentage).isEqualTo("1 (50.00 %)")
        assertThat(statistics.failedRequests).containsExactly(failedRequestText)
        assertThat(statistics.totalCalls).isEqualTo(2)
        assertThat(statistics.totalCallsWithPercentage).isEqualTo("2 (100.00 %)")
        assertThat(statistics.specialCommandsCount).isEqualTo(3)  // updated
        assertThat(statistics.totalCallsWithSpecialCommands).isEqualTo(5) // updated
    }
}
