package com.dv.telegram.util

import org.apache.logging.log4j.kotlin.Logging
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

object DateUtils : Logging {

    private val FILE_NAME_FORMAT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss") // only primitives and Strings can be const

    fun getDateTimeInFileNameFormat(temporal: TemporalAccessor): String = // excludes : in the file name
        FILE_NAME_FORMAT_FORMATTER.format(temporal)

    fun getCurrentDateTimeInFileNameFormat(): String {
        val currentTime = ZonedDateTime.now()

        return getDateTimeInFileNameFormat(currentTime)
    }
}