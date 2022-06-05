package com.dv.telegram.data

data class WikiPageData(
    val name: String,
    val url: String,
    val wordsString: String,
    val words: List<String>
) {
    fun isPresentIn(text: String): Boolean {
        if (text.isBlank()) {
            return false
        }

        for (word in words) {
            if (text.contains(word)) {
                return true
            }
        }

        return false
    }

    fun getOneLineAnswer() = "$name — $url"

    fun getMultiLineAnswer() = "— $name — $url"
}
