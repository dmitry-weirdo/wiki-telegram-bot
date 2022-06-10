package com.dv.telegram.notion

import com.dv.telegram.exception.CommandException
import org.apache.logging.log4j.kotlin.Logging
import java.util.concurrent.atomic.AtomicBoolean

object NotionOperationBlocker : Logging { // see https://stackoverflow.com/questions/51834996/singleton-class-in-kotlin

    private val operationRunning = AtomicBoolean(false)

    fun startOperation() {
        if (operationRunning.get()) {
            throw CommandException("Другая загрузка находится в процессе. Пожалуйста, подождите несколько минут!")
        }

        operationRunning.set(true)
        logger.info("Operation started.")
    }

    fun stopOperation() {
        if (operationRunning.get()) {
            operationRunning.set(false)

            logger.info("Operation stopped.")
        }
    }
}
