package com.dv.telegram.data

object DataUtils {
    private const val WORDS_SEPARATOR = ","

    @JvmOverloads
    @JvmStatic
    fun parseWords(wordsString: String?, separator: String = WORDS_SEPARATOR): List<String> {
        if (wordsString.isNullOrBlank()) {
            return listOf()
        }

        val words = wordsString
            .lowercase()
            .split(separator)

        return words
            .filter { it.isNotBlank() } // prevent empty strings
            .map { it.lowercase().trim() }
            .toList()
    }
}
