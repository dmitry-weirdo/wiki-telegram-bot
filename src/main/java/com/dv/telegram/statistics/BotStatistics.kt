package com.dv.telegram.statistics

import com.dv.telegram.MessageProcessingResult
import lombok.Data
import java.time.ZonedDateTime

@Data
class BotStatistics {
    val startTime: ZonedDateTime = ZonedDateTime.now()
    var specialCommandsCount = 0L
    var successfulRequestsCount = 0L
    var failedRequestsCount = 0L
    var failedRequests: MutableSet<String> = LinkedHashSet() // todo: add String -> count map if required

    companion object {
        private const val COUNT_PERCENTAGE_FORMAT = "%d (%.02f %%)"
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

    private fun addFailedRequest(failedRequest: String) {
        failedRequests.add(failedRequest)
    }

    fun clearFailedRequests() {
        failedRequests.clear()
    }
}
