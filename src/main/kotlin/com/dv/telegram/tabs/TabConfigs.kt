package com.dv.telegram.tabs

import com.dv.telegram.util.JacksonUtils
import java.io.File

data class TabConfigs(
    val commandTabs: List<TabConfig> = listOf(),
    val dataTabs: List<TabConfig> = listOf()
)

object TestTabConfigs {

    @JvmStatic
    fun main(args: Array<String>) {
        val tabConfigs = TabConfigs(
            commandTabs = listOf(
                TabConfig(
                    "Дюся болталка",
                    TabFormat.COMMANDS,
                    TabType.COMMANDS
                ),
                TabConfig(
                    "Общая болталка",
                    TabFormat.COMMANDS,
                    TabType.COMMANDS
                )
            ),

            dataTabs = listOf(
                TabConfig(
                    "Страницы вики",
                    TabFormat.WIKI_PAGES,
                    TabType.WIKI_PAGES
                ),
                TabConfig(
                    "UAHelp YouTube",
                    TabFormat.COMMANDS,
                    TabType.YOUTUBE_VIDEOS
                ),
                TabConfig(
                    "Список чатов по городам Германии",
                    TabFormat.CHATS,
                    TabType.CITY_CHATS
                ),
                TabConfig(
                    "Список чатов по странам",
                    TabFormat.CHATS,
                    TabType.COUNTRY_CHATS
                )
            )
        )

        val outputFilePath = "C:\\java\\wiki-telegram-bot\\.ignoreme\\test-bot-config.json"
        JacksonUtils.serializeToFile(outputFilePath, tabConfigs, true)

        val parsed = JacksonUtils.parse(File(outputFilePath), TabConfigs::class.java)
        println(parsed)

/*
        // just the enum serialization is working!
        val outputFilePath = "C:\\java\\wiki-telegram-bot\\.ignoreme\\test-enum.json"
        val tabFormat = TabFormat.WIKI_PAGES
        JacksonUtils.serializeToFile(outputFilePath, tabFormat, true)

        val parsed = JacksonUtils.parse(File(outputFilePath), TabFormat::class.java)
        println(parsed)
*/
    }
}
