package com.dv.telegram.exception

class CommandException : RuntimeException {
    val errorMessages: List<String>
    val useMarkdownInResponse: Boolean

    constructor(message: String, useMarkdownInResponse: Boolean = false) : super(message) {
        errorMessages = listOf(message)
        this.useMarkdownInResponse = useMarkdownInResponse
    }

    constructor(messages: List<String>, useMarkdownInResponse: Boolean = false) : super(messages[0]) {
        errorMessages = messages
        this.useMarkdownInResponse = useMarkdownInResponse
    }

    constructor(message: String, cause: Throwable?, useMarkdownInResponse: Boolean = false) : super(message, cause) {
        errorMessages = listOf(message)
        this.useMarkdownInResponse = useMarkdownInResponse
    }

    constructor(messages: List<String>, cause: Throwable?, useMarkdownInResponse: Boolean = false) : super(messages[0], cause) {
        errorMessages = messages
        this.useMarkdownInResponse = useMarkdownInResponse
    }
}
