package com.dv.telegram

import org.apache.logging.log4j.kotlin.Logging

class WikiBotsContext : Logging { // context that holds links of all bots executed in this Main class
    val bots: MutableList<WikiBot> = mutableListOf()

    fun addBot(bot: WikiBot) {
        synchronized(bots) {
            bots.add(bot)
        }
    }
}
