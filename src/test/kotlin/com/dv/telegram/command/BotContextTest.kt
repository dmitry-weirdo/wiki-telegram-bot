package com.dv.telegram.command

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BotContextTest {

    private lateinit var botContext: BotContext

    @BeforeEach
    fun setUp() {
        botContext = BotContext()
    }

    // ===== put() method tests =====

    @Test
    fun testPutStoresStringValue() {
        val key = "key1"
        val value = "value1"
        botContext.put(key, value)

        assertThat(botContext.get(key)).isEqualTo(value)
    }

    @Test
    fun testPutStoresIntValue() {
        val key = "number"
        val value = 42
        botContext.put(key, value)

        assertThat(botContext.get(key)).isEqualTo(value)
    }

    @Test
    fun testPutStoresListValue() {
        val key = "list"
        val value = listOf("item1", "item2", "item3")
        botContext.put(key, value)

        assertThat(botContext.get(key)).isEqualTo(value)
    }

    @Test
    fun testPutStoreBooleanValue() {
        val key = "flag"
        val value = true
        botContext.put(key, value)

        assertThat(botContext.get(key)).isEqualTo(value)
    }

    @Test
    fun testPutOverwritesExistingValue() {
        val key = "key"
        val oldValue = "oldValue"
        val newValue = "newValue"

        botContext.put(key, oldValue)
        assertThat(botContext.get(key)).isEqualTo(oldValue)

        botContext.put(key, newValue)

        assertThat(botContext.get(key)).isEqualTo(newValue)
    }

    @Test
    fun testPutMultipleValuesWithDifferentKeys() {
        val key1 = "key1"
        val value1 = "value1"
        val key2 = "key2"
        val value2 = 42
        val key3 = "key3"
        val value3 = true

        botContext.put(key1, value1)
        botContext.put(key2, value2)
        botContext.put(key3, value3)

        assertThat(botContext.get(key1)).isEqualTo(value1)
        assertThat(botContext.get(key2)).isEqualTo(value2)
        assertThat(botContext.get(key3)).isEqualTo(value3)
    }

    // ===== untyped get() method tests =====

    @Test
    fun testUntypedGetReturnsStoredValue() {
        val key = "myKey"
        val value = "myValue"
        botContext.put(key, value)

        val result = botContext.get(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testUntypedGetReturnsNullForNonExistentKey() {
        val result = botContext.get("nonExistent")

        assertThat(result).isNull()
    }

    @Test
    fun testUntypedGetReturnsActualType() {
        val key = "list"
        val item1 = "a"
        val item2 = "b"
        val item3 = "c"
        val list = listOf(item1, item2, item3)
        botContext.put(key, list)

        val result = botContext.get(key)

        assertThat(result).isInstanceOf(List::class.java)
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun testUntypedGetAfterPutWithDifferentValues() {
        val key = "key"
        val intValue = 123
        val stringValue = "string"

        botContext.put(key, intValue)
        assertThat(botContext.get(key)).isEqualTo(intValue)

        botContext.put(key, stringValue)
        assertThat(botContext.get(key)).isEqualTo(stringValue)
    }

    // ===== typed get<T>() method tests =====

    @Test
    fun testTypedGetReturnsStringValue() {
        val key = "name"
        val value = "Alice"
        botContext.put(key, value)

        val result: String? = botContext.get<String>(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testTypedGetReturnsIntValue() {
        val key = "age"
        val value = 30
        botContext.put(key, value)

        val result: Int? = botContext.get<Int>(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testTypedGetReturnsLongValue() {
        val key = "timestamp"
        val value = 1234567890L
        botContext.put(key, value)

        val result: Long? = botContext.get<Long>(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testTypedGetReturnsBooleanValue() {
        val key = "isActive"
        val value = true
        botContext.put(key, value)

        val result: Boolean? = botContext.get<Boolean>(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testTypedGetReturnsListOfStrings() {
        val key = "fruits"
        val apple = "apple"
        val banana = "banana"
        val cherry = "cherry"
        val stringList = listOf(apple, banana, cherry)
        botContext.put(key, stringList)

        val result: List<String>? = botContext.get<List<String>>(key)

        assertThat(result).isEqualTo(stringList)
        assertThat(result).hasSize(3)
        assertThat(result).containsExactly(apple, banana, cherry)
    }

    @Test
    fun testTypedGetReturnsListOfInts() {
        val key = "numbers"
        val num1 = 1
        val num2 = 2
        val num3 = 3
        val num4 = 4
        val num5 = 5
        val intList = listOf(num1, num2, num3, num4, num5)
        botContext.put(key, intList)

        val result: List<Int>? = botContext.get<List<Int>>(key)

        assertThat(result).isEqualTo(intList)
        assertThat(result).hasSize(5)
    }

    @Test
    fun testTypedGetReturnsNullForNonExistentKey() {
        val result: String? = botContext.get<String>("missing")

        assertThat(result).isNull()
    }

    @Test
    fun testTypedGetReturnsNullOnTypeMismatch() {
        val key = "value"
        val stringValue = "stringValue"
        botContext.put(key, stringValue)

        val result: Int? = botContext.get<Int>(key)

        assertThat(result).isNull()
    }

    @Test
    fun testTypedGetThrowsExceptionOnCastingWhenListTypeDoesntMatch() {
        val key = "numbers"
        val intList = listOf(1, 2, 3)
        botContext.put(key, intList)

        val result: List<String>? = botContext.get<List<String>>(key)

//        assertThat(result).isNull()
        // List<Int> will be returned as List<String>
        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(List::class.java)

        // but casting Int to String will fail with ClassCastException
        assertThatThrownBy {
            val string = result!![0]
            print("result[0] as string: $string")
        }
            .isInstanceOf(ClassCastException::class.java)
    }

    @Test
    fun testTypedGetWithComplexObject() {
        data class Person(val name: String, val age: Int)

        val key = "person"
        val name = "Bob"
        val age = 25
        val person = Person(name = name, age = age)

        botContext.put(key, person)

        val result: Person? = botContext.get<Person>(key)

        assertThat(result).isEqualTo(person)
        assertThat(result?.name).isEqualTo(name)
        assertThat(result?.age).isEqualTo(age)
    }

    // ===== isPresent() method tests =====

    @Test
    fun testIsPresentReturnsTrueWhenKeyExists() {
        val key = "key"
        val value = "value"
        botContext.put(key, value)

        val result = botContext.isPresent(key)

        assertThat(result).isTrue
    }

    @Test
    fun testIsPresentReturnsFalseWhenKeyDoesNotExist() {
        val result = botContext.isPresent("nonExistent")

        assertThat(result).isFalse
    }

    @Test
    fun testIsPresentReturnsFalseAfterContextInitialization() {
        val result = botContext.isPresent("anything")

        assertThat(result).isFalse
    }

    @Test
    fun testIsPresentWithMultipleKeys() {
        val key1 = "key1"
        val key2 = "key2"
        val key3 = "key3"
        botContext.put(key1, "value1")
        botContext.put(key2, "value2")

        assertThat(botContext.isPresent(key1)).isTrue
        assertThat(botContext.isPresent(key2)).isTrue
        assertThat(botContext.isPresent(key3)).isFalse
    }

    @Test
    fun testIsPresentAfterValueOverwrite() {
        val key = "key"
        val value1 = "value1"
        val value2 = "value2"

        botContext.put(key, value1)
        assertThat(botContext.isPresent(key)).isTrue

        botContext.put(key, value2)

        assertThat(botContext.isPresent(key)).isTrue
    }

    @Test
    fun testIsPresentWithDifferentDataTypes() {
        val stringKey = "string"
        val stringValue = "value"
        val intKey = "int"
        val intValue = 42
        val listKey = "list"
        val item1 = "a"
        val item2 = "b"
        val listValue = listOf(item1, item2)
        val booleanKey = "boolean"
        val booleanValue = false

        botContext.put(stringKey, stringValue)
        botContext.put(intKey, intValue)
        botContext.put(listKey, listValue)
        botContext.put(booleanKey, booleanValue)

        assertThat(botContext.isPresent(stringKey)).isTrue
        assertThat(botContext.isPresent(intKey)).isTrue
        assertThat(botContext.isPresent(listKey)).isTrue
        assertThat(botContext.isPresent(booleanKey)).isTrue
    }

    // ===== Integration tests =====

    @Test
    fun testCompleteWorkflowPutGetIsPresent() {
        val usernameKey = "username"
        val aliceValue = "alice"
        val bobValue = "bob"

        assertThat(botContext.isPresent(usernameKey)).isFalse

        botContext.put(usernameKey, aliceValue)
        assertThat(botContext.isPresent(usernameKey)).isTrue

        assertThat(botContext.get(usernameKey)).isEqualTo(aliceValue)

        val username: String? = botContext.get<String>(usernameKey)
        assertThat(username).isEqualTo(aliceValue)

        botContext.put(usernameKey, bobValue)
        assertThat(botContext.isPresent(usernameKey)).isTrue
        assertThat(botContext.get(usernameKey)).isEqualTo(bobValue)

        val updatedUsername: String? = botContext.get<String>(usernameKey)
        assertThat(updatedUsername).isEqualTo(bobValue)
    }

    @Test
    fun testContextIsolation() {
        val context1 = BotContext()
        val context2 = BotContext()
        val key = "key"
        val value1 = "value1"
        val value2 = "value2"

        context1.put(key, value1)
        context2.put(key, value2)

        assertThat(context1.get(key)).isEqualTo(value1)
        assertThat(context2.get(key)).isEqualTo(value2)
    }

    @Test
    fun testMixedTypesInContext() {
        val nameKey = "name"
        val nameValue = "John"
        val ageKey = "age"
        val ageValue = 28
        val activeKey = "active"
        val activeValue = true
        val tagsKey = "tags"
        val tag1 = "admin"
        val tag2 = "user"
        val tagsValue = listOf(tag1, tag2)

        botContext.put(nameKey, nameValue)
        botContext.put(ageKey, ageValue)
        botContext.put(activeKey, activeValue)
        botContext.put(tagsKey, tagsValue)

        assertThat(botContext.isPresent(nameKey)).isTrue
        assertThat(botContext.isPresent(ageKey)).isTrue
        assertThat(botContext.isPresent(activeKey)).isTrue
        assertThat(botContext.isPresent(tagsKey)).isTrue

        val name: String? = botContext.get<String>(nameKey)
        val age: Int? = botContext.get<Int>(ageKey)
        val active: Boolean? = botContext.get<Boolean>(activeKey)
        val tags: List<String>? = botContext.get<List<String>>(tagsKey)

        assertThat(name).isEqualTo(nameValue)
        assertThat(age).isEqualTo(ageValue)
        assertThat(active).isEqualTo(activeValue)
        assertThat(tags).containsExactly(tag1, tag2)
    }

    // ===== getOrFail() method tests (non-typed) =====

    @Test
    fun testUntypedGetOrFailReturnsStoredValue() {
        val key = "key"
        val value = "value"
        botContext.put(key, value)

        val result = botContext.getOrFail(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testUntypedGetOrFailThrowsExceptionForMissingKey() {
        val missingKey = "nonExistent"

        assertThatThrownBy {
            botContext.getOrFail(missingKey)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Key 'nonExistent' not found in BotContext")
    }

    @Test
    fun testUntypedGetOrFailReturnsCorrectType() {
        val key = "list"
        val item1 = "a"
        val item2 = "b"
        val item3 = "c"
        val list = listOf(item1, item2, item3)
        botContext.put(key, list)

        val result = botContext.getOrFail(key)

        assertThat(result).isInstanceOf(List::class.java)
        assertThat(result).isEqualTo(list)
    }

    @Test
    fun testUntypedGetOrFailWithMultipleCalls() {
        val key1 = "key1"
        val value1 = "value1"
        val key2 = "key2"
        val value2 = 42

        botContext.put(key1, value1)
        botContext.put(key2, value2)

        val result1 = botContext.getOrFail(key1)
        val result2 = botContext.getOrFail(key2)

        assertThat(result1).isEqualTo(value1)
        assertThat(result2).isEqualTo(value2)
    }

    // ===== getOrFail<T>() method tests (typed) =====

    @Test
    fun testTypedGetOrFailReturnsStringValue() {
        val key = "name"
        val value = "Alice"
        botContext.put(key, value)

        val result: String = botContext.getOrFail<String>(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testTypedGetOrFailReturnsIntValue() {
        val key = "age"
        val value = 30
        botContext.put(key, value)

        val result: Int = botContext.getOrFail<Int>(key)

        assertThat(result).isEqualTo(value)
    }

    @Test
    fun testTypedGetOrFailReturnsListOfStrings() {
        val key = "fruits"
        val apple = "apple"
        val banana = "banana"
        val cherry = "cherry"
        val stringList = listOf(apple, banana, cherry)
        botContext.put(key, stringList)

        val result: List<String> = botContext.getOrFail<List<String>>(key)

        assertThat(result).isEqualTo(stringList)
        assertThat(result).hasSize(3)
        assertThat(result).containsExactly(apple, banana, cherry)
    }

    @Test
    fun testTypedGetOrFailThrowsExceptionForMissingKey() {
        val missingKey = "missing"

        assertThatThrownBy {
            botContext.getOrFail<String>(missingKey)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Key 'missing' not found in BotContext")
    }

    @Test
    fun testTypedGetOrFailThrowsExceptionOnTypeMismatch() {
        val key = "value"
        val value = "stringValue"

        botContext.put(key, value)

        assertThatThrownBy {
            botContext.getOrFail<Int>(key)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Key 'value' not found in BotContext or value is not of type Int")
    }

    @Test
    fun testTypedGetOrFailThrowsExceptionOnCastingWhenListTypeDoesntMatch() {
        val key = "numbers"
        val intList = listOf(1, 2, 3)

        botContext.put(key, intList)

        // List<Int> will be returned as List<String>

//        assertThatThrownBy {
//            val result = botContext.getOrFail<List<String>>(key)
//        }
//            .isInstanceOf(IllegalStateException::class.java)
//            .hasMessageContaining("not of type List")

        val result = botContext.getOrFail<List<String>>(key)
        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(List::class.java)

        // but casting Int to String will fail with ClassCastException
        assertThatThrownBy {
            val string = result[0]
            print("result[0] as string: $string")
        }
            .isInstanceOf(ClassCastException::class.java)
    }

    @Test
    fun testTypedGetOrFailWithComplexObject() {
        data class Person(val name: String, val age: Int)

        val key = "person"
        val name = "Bob"
        val age = 25
        val person = Person(name = name, age = age)

        botContext.put(key, person)

        val result: Person = botContext.getOrFail<Person>(key)

        assertThat(result).isEqualTo(person)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.age).isEqualTo(age)
    }
}
