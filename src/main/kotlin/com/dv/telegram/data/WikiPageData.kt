package com.dv.telegram.data

data class WikiPageData(
    val name: String,
    val url: String,
    val wordsString: String,
    val words: List<String>
) : BotAnswerData {
    override fun isPresentIn(text: String) = isPresentIn(text, words)

    override fun getMatch(text: String) = getMatch(text, words)

    fun getOneLineAnswer() = "$name — $url"

    fun getMultiLineAnswer() = "▫ $name — $url" // todo: use configurable bullet
}
