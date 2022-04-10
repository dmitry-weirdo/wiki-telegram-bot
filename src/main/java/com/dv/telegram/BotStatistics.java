package com.dv.telegram;

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

    public long getTotalCalls() {
        return successfulRequestsCount + failedRequestsCount;
    }

    public long getTotalCallsWithSpecialCommands() {
        return specialCommandsCount + getTotalCalls();
    }

    public void addFailedRequest(String failedRequest) {
        failedRequests.add(failedRequest);
    }

    public void clearFailedRequests() {
        failedRequests.clear();
    }
}
