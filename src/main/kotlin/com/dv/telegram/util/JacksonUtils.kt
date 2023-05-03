package com.dv.telegram.util

import com.dv.telegram.WikiBotConfig
import com.dv.telegram.WikiBotConfigs
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

object JacksonUtils : Logging {

    fun <T> parse(file: File, clazz: Class<T>): T {
        return try {
            val mapper = createObjectMapper()
            mapper.readValue(file, clazz)
        }
        catch (e: IOException) {
            throw handleError(e, "Error on parsing file ${file.path} to class ${clazz.name}")
        }
    }

    fun parseConfig(filePath: String): WikiBotConfig {
        val file = File(filePath)
        val config = parse(file, WikiBotConfig::class.java)
        logger.debug("Successfully parsed config from file $filePath")
        //        logger.debug("Config: {}", config);
        return config
    }

    fun parseConfigs(filePath: String): WikiBotConfigs {
        val file = File(filePath)
        val configs = parse(file, WikiBotConfigs::class.java)
        logger.debug("Successfully parsed configs from file $filePath")
        //        logger.debug("Configs: {}", configs);
        return configs
    }

    fun serialize(file: File, obj: Any) {
        try {
            val mapper = createObjectMapper()
            mapper.writeValue(file, obj)
        }
        catch (e: IOException) {
            throw handleError(e, "Error on writing object of class ${obj.javaClass.name} to file ${file.path}")
        }
    }

    fun serializeToString(obj: Any, prettyPrint: Boolean = false): String {
        return try {
            val mapper = createObjectMapper(prettyPrint)
            mapper.writeValueAsString(obj)
        }
        catch (e: JsonProcessingException) {
            throw handleError(e, "Error on writing object of class ${obj.javaClass.name} to String")
        }
    }

    fun serializeToFile(filePath: String, obj: Any, prettyPrint: Boolean) {
        try {
            val serializedObject = serializeToString(obj, prettyPrint)

            val file = File(filePath)
            FileUtils.writeStringToFile(file, serializedObject, StandardCharsets.UTF_8) // todo: probably use some Kotlin's internal writer
            logger.debug("Successfully serialized object of class ${obj.javaClass.name} to file $filePath.")
            logger.debug("Serialized object: $serializedObject")
        }
        catch (e: IOException) {
            throw handleError(e, "Error on writing object of class ${obj.javaClass.name} to file $filePath")
        }
    }

    private fun handleError(e: IOException, errorMessage: String): RuntimeException {
        logger.error(errorMessage, e)
        return RuntimeException(errorMessage, e)
    }

    private fun createObjectMapper(prettyPrint: Boolean = false): ObjectMapper {
        val kotlinModule: KotlinModule = KotlinModule
            .Builder()
            .enable(KotlinFeature.StrictNullChecks)
            .build()

        val mapper = ObjectMapper() // serialize LocalDateTime not as object, but as date string
            .registerModule(JavaTimeModule())
            .registerModule(kotlinModule)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false) // do not lose timezone when de-serializing OffsetDateTime
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // because of _id / id clash in /get-summary response, see https://github.com/OpenAPITools/openapi-generator/issues/8291
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true) // to not fail on "type": 0 in "voc-107263" in get-stats-overview-80523.json. See https://stackoverflow.com/a/51407361/8534088

        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
        }

        return mapper
    }
}
