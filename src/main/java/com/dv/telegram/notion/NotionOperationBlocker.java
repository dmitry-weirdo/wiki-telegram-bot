package com.dv.telegram.notion;

import com.dv.telegram.exception.CommandException;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class NotionOperationBlocker {

    private final AtomicBoolean operationRunning = new AtomicBoolean(false);

    private static NotionOperationBlocker instance;

    private NotionOperationBlocker() {
    }

    public static NotionOperationBlocker getInstance() {
        if (instance == null) {
            instance = new NotionOperationBlocker();
        }

        return instance;
    }

    public void startOperation() {
        if (operationRunning.get()) {
            throw new CommandException("Другая загрузка находится в процессе. Пожалуйста, подождите несколько минут!");
        }

        operationRunning.set(true);
        log.info("Operation started.");
    }

    public void stopOperation() {
        if (operationRunning.get()) {
            operationRunning.set(false);

            log.info("Operation stopped.");
        }
    }
}
