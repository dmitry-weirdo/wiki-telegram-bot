package com.dv.telegram.notion

import com.dv.telegram.exception.CommandException
import com.dv.telegram.util.DateUtils
import org.apache.logging.log4j.kotlin.Logging
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicBoolean

object NotionOperationBlocker : Logging { // see https://stackoverflow.com/questions/51834996/singleton-class-in-kotlin

    private val operationRunning = AtomicBoolean(false)
    private var operationStartTime: ZonedDateTime? = null
    private var operationTimeoutTime: ZonedDateTime? = null

    fun startOperation(timeoutMinutes: Int) {
        val currentTime = ZonedDateTime.now()

        if (operationRunning.get()) {
            val currentTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(currentTime)
            val startTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(operationStartTime!!)
            val timeoutTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(operationTimeoutTime!!)

            // timeout not yet reached
            if (currentTime.isBefore(operationTimeoutTime)) {
                logger.info("Other operation is running since $startTimeFormatted, and timeout $timeoutTimeFormatted is not yet reached. Current time: $currentTimeFormatted. Forbidding to start a new operation!")

                throw CommandException(
                    "Другая загрузка находится в процессе, начиная с $startTimeFormatted." +
                        "\nПожалуйста, подождите несколько минут до завершения загрузки или до наступления таймаута в $timeoutTimeFormatted!"
                )
            }

            // timeout reached -> allow to restart the operation
            logger.info("Other operation is running since $startTimeFormatted, but timeout $timeoutTimeFormatted is reached. Current time: $currentTimeFormatted. Starting a new operation!")
        }

        operationStartTime = ZonedDateTime.now()
        operationTimeoutTime = operationStartTime!!.plusMinutes(timeoutMinutes.toLong())

        operationRunning.set(true)

        val startTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(operationStartTime!!)
        val timeoutTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(operationTimeoutTime!!)

        logger.info("Operation started at $startTimeFormatted. Operation will be timed out at $timeoutTimeFormatted.")
    }

    fun stopOperation() {
        if (operationRunning.get()) {
            val currentTime = ZonedDateTime.now()

            val currentTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(currentTime)
            val startTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(operationStartTime!!)
            val timeoutTimeFormatted = DateUtils.getDateTimeInNotionOperationFormat(operationTimeoutTime!!)

            operationStartTime = null
            operationTimeoutTime = null

            operationRunning.set(false)

            logger.info("Operation stopped at $currentTimeFormatted. Operation was started at $startTimeFormatted and had a timeout at $timeoutTimeFormatted.")
        }
    }
}
