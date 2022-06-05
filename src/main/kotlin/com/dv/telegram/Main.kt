package com.dv.telegram

import com.dv.telegram.config.SettingValidationException
import com.dv.telegram.util.WikiBotUtils
import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class TestKotlinMain {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Kotlin is Working from a class!")
        }
    }
}

class Main : Logging {

    companion object X: Logging {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val wikiBotConfigs = WikiBotUtils.readConfigs()

                val threadsCount = wikiBotConfigs.configs.size
                logger.info("Total bot configs: $threadsCount.")

                val callableTasks = wikiBotConfigs
                    .configs
                    .map { this.createCallableTask(it) }

                val executorService = Executors.newFixedThreadPool(threadsCount)
                executorService.invokeAll(callableTasks) // todo: this currently does not stop Main on exception in the thread
            }
            catch (e: InterruptedException) {
                logger.debug("============================================")
                logger.error("executorService.invokeAll was interrupted", e)
                throw RuntimeException(e)
            }
        }

        private fun createCallableTask(config: WikiBotConfig): Callable<String> {
            return Callable {
                try {
                    val botData = GoogleSheetLoader.readGoogleSheet(config)

                    val wikiBot = WikiBot(config, botData)

                    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
                    botsApi.registerBot(wikiBot)
                    logger.info(
                        "(Kotlin!) The bot \"${wikiBot.botUsername}\" has started on \"${wikiBot.environmentName}\" environment!",
                    )

                    "The bot ${wikiBot.botUsername} has started on \"${wikiBot.environmentName}\" environment!"
                }
                catch (e: TelegramApiException) {
                    logger.error("Error when starting the WikiBot", e)
                    throw RuntimeException(e)
                }
                catch (e: SettingValidationException) {
                    logger.error("Settings validation error when starting the WikiBot", e)
                    throw RuntimeException(e)
                }
                catch (e: Exception) {
                    logger.error("Unknown exception when starting the WikiBot", e)
                    throw RuntimeException(e)
                }
            }
        }
    }
}
