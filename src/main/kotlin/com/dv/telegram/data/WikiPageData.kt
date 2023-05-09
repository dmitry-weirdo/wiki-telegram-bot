package com.dv.telegram.data

data class WikiPageData(
    val name: String,
    val url: String,
    val wordsString: String,
    val words: List<String>
) : BotAnswerData {
    override fun isPresentIn(text: String) = isPresentIn(text, words)

    override fun getMatch(text: String) = getMatch(text, words)

    // todo: for optimization, we can pre-fill the answers based on tabConfig, as in ChatData
    fun getAnswerWithoutBullet() = "$name — $url"

    fun getAnswerWithBullet(bullet: String) = "$bullet $name — $url"
}
