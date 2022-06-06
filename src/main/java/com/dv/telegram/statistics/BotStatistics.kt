package com.dv.telegram.statistics;

import com.dv.telegram.MessageProcessingResult;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class BotStatistics {

    public BotStatistics() {
        startTime = ZonedDateTime.now();
    }

    public final ZonedDateTime startTime;
    public long specialCommandsCount = 0;
    public long successfulRequestsCount = 0;
    public long failedRequestsCount = 0;

    public Set<String> failedRequests = new LinkedHashSet<>(); // todo: add String -> count map if required

    private static final String COUNT_PERCENTAGE_FORMAT = "%d (%.02f %%)";

    public void update(String text, MessageProcessingResult processingResult) {
        if (!processingResult.getMessageIsForTheBot()) {
            return;
        }

        if (processingResult.isSpecialCommand()) {
            specialCommandsCount++;
        }
        else if (processingResult.getAnswerIsFound()) {
            successfulRequestsCount++;
        }
        else {
            failedRequestsCount++;
            addFailedRequest(text);
        }
    }

    public String getSuccessfulRequestsCountWithPercentage() {
        double successfulRequestsPercentage = (getTotalCalls() == 0) ? 0d : getSuccessfulRequestsPercentage();
        return String.format(COUNT_PERCENTAGE_FORMAT, successfulRequestsCount, successfulRequestsPercentage);
    }

    public String getFailedRequestsCountWithPercentage() {
        double failedRequestPercentage = (getTotalCalls() == 0) ? 0d : (100 - getSuccessfulRequestsPercentage());
        return String.format(COUNT_PERCENTAGE_FORMAT, failedRequestsCount, failedRequestPercentage);
    }

    public String getTotalCallsWithPercentage() {
        return String.format(COUNT_PERCENTAGE_FORMAT, getTotalCalls(), 100d);
    }

    private double getSuccessfulRequestsPercentage() {
        return 100 * successfulRequestsCount / (double) getTotalCalls();
    }

    public long getTotalCalls() {
        return successfulRequestsCount + failedRequestsCount;
    }

    public long getTotalCallsWithSpecialCommands() {
        return specialCommandsCount + getTotalCalls();
    }

    private void addFailedRequest(String failedRequest) {
        failedRequests.add(failedRequest);
    }

    public void clearFailedRequests() {
        failedRequests.clear();
    }
}
