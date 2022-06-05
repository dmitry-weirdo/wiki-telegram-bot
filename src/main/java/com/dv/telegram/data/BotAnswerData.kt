package com.dv.telegram.data

interface BotAnswerData {
    fun isPresentIn(text: String): Boolean
}
