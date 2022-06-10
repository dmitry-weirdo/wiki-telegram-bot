package com.dv.telegram.command

data class SpecialCommandResponse(
    val response: String?,
    val useMarkdownInResponse: Boolean
) {
    fun hasResponse(): Boolean {
        return response?.isNotBlank() == true
    }

    companion object {
        fun noResponse(): SpecialCommandResponse = SpecialCommandResponse(null, false)

        fun withResponse(response: String, useMarkdownInResponse: Boolean): SpecialCommandResponse {
            require(response.isNotBlank()) { "response cannot be blank." }

            return SpecialCommandResponse(response, useMarkdownInResponse)
        }
    }
}
