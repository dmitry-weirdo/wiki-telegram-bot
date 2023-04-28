package com.dv.telegram.statistics

import com.dv.telegram.MessageProcessingResult
import java.time.ZonedDateTime

data class BotStatistics(
    val startTime: ZonedDateTime = ZonedDateTime.now(),
    var specialCommandsCount: Long = 0L,
    var successfulRequestsCount: Long = 0L,
    var failedRequestsCount: Long = 0L,
    var successfulRequests: MutableSet<String> = LinkedHashSet(), // todo: add String -> count map if required
    var failedRequests: MutableSet<String> = LinkedHashSet() // todo: add String -> count map if required
) {
    companion object {
        private const val COUNT_PERCENTAGE_FORMAT = "%d (%.02f %%)"

        fun getAggregateStatistics(statisticsList: List<BotStatistics>): BotStatistics {
            val allSuccessfulRequests = mutableSetOf<String>()
            statisticsList.forEach {
                allSuccessfulRequests.addAll(it.successfulRequests)
            }

            val allFailedRequests = mutableSetOf<String>()
            statisticsList.forEach {
                allFailedRequests.addAll(it.failedRequests)
            }

            return BotStatistics(
                statisticsList[0].startTime, // fill something
                statisticsList.sumOf { it.specialCommandsCount },
                statisticsList.sumOf { it.successfulRequestsCount },
                statisticsList.sumOf { it.failedRequestsCount },
                allSuccessfulRequests,
                allFailedRequests
            )
        }
    }

    fun update(text: String, processingResult: MessageProcessingResult) {
        if (!processingResult.messageIsForTheBot) {
            return
        }

        if (processingResult.isSpecialCommand) {
            specialCommandsCount++
        }
        else if (processingResult.answerIsFound) {
            successfulRequestsCount++
            addSuccessfulRequest(text)
        }
        else {
            failedRequestsCount++
            addFailedRequest(text)
        }
    }

    val successfulRequestsCountWithPercentage: String
        get() {
            val successfulRequestsPercentage = if (totalCalls == 0L) 0.0 else successfulRequestsPercentage
            return String.format(COUNT_PERCENTAGE_FORMAT, successfulRequestsCount, successfulRequestsPercentage)
        }

    val failedRequestsCountWithPercentage: String
        get() {
            val failedRequestPercentage = if (totalCalls == 0L) 0.0 else (100 - successfulRequestsPercentage)
            return String.format(COUNT_PERCENTAGE_FORMAT, failedRequestsCount, failedRequestPercentage)
        }

    val totalCallsWithPercentage: String
        get() = String.format(COUNT_PERCENTAGE_FORMAT, totalCalls, 100.0)

    private val successfulRequestsPercentage: Double
        get() = 100 * successfulRequestsCount / totalCalls.toDouble()

    val totalCalls: Long
        get() = successfulRequestsCount + failedRequestsCount

    val totalCallsWithSpecialCommands: Long
        get() = specialCommandsCount + totalCalls

    private fun addSuccessfulRequest(successfulRequest: String) {
        successfulRequests.add(successfulRequest)
    }

    private fun addFailedRequest(failedRequest: String) {
        failedRequests.add(failedRequest)
    }

    fun clearSuccessfulRequests() {
        successfulRequests.clear()
    }

    fun clearFailedRequests() {
        failedRequests.clear()
    }
}
