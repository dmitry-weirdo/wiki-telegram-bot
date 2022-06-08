package com.dv.telegram.data

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class DataUtilsTest {

    @Test
    fun testBlankStrings() {
        val fromNull = DataUtils.parseWords(null)
        Assertions.assertThat(fromNull).isEmpty()

        val fromEmptyString = DataUtils.parseWords("")
        Assertions.assertThat(fromEmptyString).isEmpty()

        val fromBlankString = DataUtils.parseWords("     \t      ")
        Assertions.assertThat(fromBlankString).isEmpty()
    }

    @Test
    fun testOneWordString() {
        val justOneWord = DataUtils.parseWords("OnE word")
        Assertions.assertThat(justOneWord).containsExactly("one word")

        val oneWordAndBlanks = DataUtils.parseWords("one word ,,   ,   ,")
        Assertions.assertThat(oneWordAndBlanks).containsExactly("one word")

        val oneWordAndBlanks2 = DataUtils.parseWords("one word   ,,   ,   ")
        Assertions.assertThat(oneWordAndBlanks2).containsExactly("one word")
    }

    @Test
    fun testMultipleWords() {
        val multipleWords = DataUtils.parseWords("FirST worD  , SeCOnD wOrd   , , third word")
        Assertions.assertThat(multipleWords).containsExactly(
            "first word",
            "second word",
            "third word",
        )

        val multipleWordsBlankAtTheEnd = DataUtils.parseWords("FirST worD  , SeCOnD wOrd   , , third word,    \t  ")
        Assertions.assertThat(multipleWordsBlankAtTheEnd).containsExactly(
            "first word",
            "second word",
            "third word",
        )
    }
}
