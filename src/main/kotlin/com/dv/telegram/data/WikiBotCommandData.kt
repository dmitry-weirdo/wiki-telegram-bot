package com.dv.telegram.data

data class WikiBotCommandData(
    val answer: String,
    val wordsString: String,
    val words: List<String>
) : BotAnswerData {
    override fun isPresentIn(text: String) = isPresentIn(text, words)

    override fun getMatch(text: String) = getMatch(text, words)

    fun getAnswerWithoutBullet() = answer

    fun getAnswerWithBullet(bullet: String) = "$bullet $answer"
}
