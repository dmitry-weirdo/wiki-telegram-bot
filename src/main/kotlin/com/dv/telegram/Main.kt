package com.dv.telegram

import com.dv.telegram.config.SettingValidationException
import com.dv.telegram.exception.WikiBotException
import com.dv.telegram.util.WikiBotUtils
import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object TestKotlinMain {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Kotlin is Working from a class!")
    }
}

class Main : Logging {

    companion object X : Logging {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val wikiBotConfigs = WikiBotUtils.readConfigs()

                val threadsCount = wikiBotConfigs.configs.size
                logger.info("Total bot configs: $threadsCount.")

                val context = WikiBotsContext()

                val callableTasks = wikiBotConfigs
                    .configs
                    .map { this.createCallableTask(it, context) }

                val executorService = Executors.newFixedThreadPool(threadsCount)
                executorService.invokeAll(callableTasks) // todo: this currently does not stop Main on exception in the thread
            }
            catch (e: InterruptedException) {
                logger.debug("============================================")
                logger.error("executorService.invokeAll was interrupted", e)
                throw WikiBotException(e)
            }
        }

        private fun createCallableTask(config: WikiBotConfig, context: WikiBotsContext): Callable<String> {
            return Callable {
                try {
                    val botTabsData = GoogleSheetLoader.readGoogleSheetTabs(config)

                    val wikiBot = WikiBot(context, config, botTabsData)

                    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
                    botsApi.registerBot(wikiBot)
                    logger.info(
                        "The bot \"${wikiBot.botUsername}\" (${wikiBot.botName}) has started on \"${wikiBot.environmentName}\" environment!"
                    )

                    "The bot ${wikiBot.botUsername} has started on \"${wikiBot.environmentName}\" environment!"
                }
                catch (e: TelegramApiException) {
                    logger.error("Error when starting the WikiBot", e)
                    throw WikiBotException(e)
                }
                catch (e: SettingValidationException) {
                    logger.error("Settings validation error when starting the WikiBot", e)
                    throw WikiBotException(e)
                }
                catch (e: Exception) {
                    logger.error("Unknown exception when starting the WikiBot", e)
                    throw WikiBotException(e)
                }
            }
        }
    }
}
