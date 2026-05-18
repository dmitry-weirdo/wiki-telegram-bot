package com.dv.telegram.util

import org.apache.logging.log4j.kotlin.Logging
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor

object DateUtils : Logging {

    private val FILE_NAME_FORMAT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss")
    private val NOTION_OPERATION_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    private const val MILLISECONDS_IN_SECOND = 1000
    private const val SECONDS_IN_MINUTE = 60

    fun getDateTimeInFileNameFormat(temporal: TemporalAccessor): String = // excludes : in the file name
        FILE_NAME_FORMAT_FORMATTER.format(temporal)

    fun getCurrentDateTimeInFileNameFormat(): String {
        val currentTime = ZonedDateTime.now()

        return getDateTimeInFileNameFormat(currentTime)
    }

    fun getDateTimeInNotionOperationFormat(temporal: TemporalAccessor) =
        NOTION_OPERATION_FORMATTER.format(temporal)

    fun getCurrentDateTimeInNotionOperationFormat(): String {
        val currentTime = ZonedDateTime.now()

        return getDateTimeInNotionOperationFormat(currentTime)
    }

    fun formatDuration(startTimeMillis: Long, endTimeMillis: Long): String {
        val millis = endTimeMillis - startTimeMillis

        return formatDuration(millis)
    }

    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / MILLISECONDS_IN_SECOND
        val minutes = totalSeconds / SECONDS_IN_MINUTE
        val seconds = totalSeconds % SECONDS_IN_MINUTE
        return String.format("%02dm %02ds", minutes, seconds)
    }

    fun formatDurationForLogs(startTime: ZonedDateTime, endTime: ZonedDateTime): String {
        val millis = ChronoUnit.MILLIS.between(startTime, endTime)
        val totalSeconds = millis / MILLISECONDS_IN_SECOND
        val minutes = totalSeconds / SECONDS_IN_MINUTE
        val seconds = totalSeconds % SECONDS_IN_MINUTE
        return String.format("%02dm %02ds", minutes, seconds)
    }

    fun formatDurationForBotMessage(startTime: ZonedDateTime, endTime: ZonedDateTime): String {
        val millis = ChronoUnit.MILLIS.between(startTime, endTime)
        val totalSeconds = millis / MILLISECONDS_IN_SECOND
        val minutes = totalSeconds / SECONDS_IN_MINUTE
        val seconds = totalSeconds % SECONDS_IN_MINUTE
        return "$minutes мин $seconds сек"
    }
}
