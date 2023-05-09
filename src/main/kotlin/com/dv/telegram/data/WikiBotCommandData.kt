package com.dv.telegram.data

data class WikiBotCommandData(
    val answer: String,
    val wordsString: String,
    val words: List<String>
) : BotAnswerData {
    override fun isPresentIn(text: String) = isPresentIn(text, words)

    override fun getMatch(text: String) = getMatch(text, words)

    fun getOneLineAnswer() = answer

    fun getMultiLineAnswer() = "▫ $answer" // todo: use configurable bullet
}
