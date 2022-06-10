package com.dv.telegram.exception

class CommandException : RuntimeException {
    val errorMessages: List<String>

    constructor(message: String) : super(message) {
        errorMessages = listOf(message)
    }

    constructor(messages: List<String>) : super(messages[0]) {
        errorMessages = messages
    }

    constructor(message: String, cause: Throwable?) : super(message, cause) {
        errorMessages = listOf(message)
    }

    constructor(messages: List<String>, cause: Throwable?) : super(messages[0], cause) {
        errorMessages = messages
    }
}
