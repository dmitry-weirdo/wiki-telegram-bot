package com.dv.telegram.command

import java.io.InputStream

data class SpecialCommandResponse(
    val response: String?,
    val useMarkdownInResponse: Boolean,
    val returnFileInResponse: Boolean,
    val responseFileName: String,
    val responseFileCaption: String,
    val responseFileContent: InputStream?
) {
    fun hasResponse(): Boolean {
        return response?.isNotBlank() == true
    }

    companion object {
        fun noResponse(): SpecialCommandResponse = SpecialCommandResponse(
            response = null,
            useMarkdownInResponse = false,
            returnFileInResponse = false,
            responseFileName = "",
            responseFileCaption = "",
            responseFileContent = null
        )

        fun withResponse(
            response: String,
            useMarkdownInResponse: Boolean,
        ): SpecialCommandResponse {
            return withResponse(response, useMarkdownInResponse, false, "", "", null)
        }

        fun withResponse(
            response: String,
            useMarkdownInResponse: Boolean,
            returnFileInResponse: Boolean,
            responseFileName: String,
            responseFileCaption: String,
            responseFileContent: InputStream?,
        ): SpecialCommandResponse {
            require(response.isNotBlank()) { "response cannot be blank." }

            return SpecialCommandResponse(
                response = response,
                useMarkdownInResponse = useMarkdownInResponse,
                returnFileInResponse = returnFileInResponse,
                responseFileName = responseFileName,
                responseFileCaption = responseFileCaption,
                responseFileContent = responseFileContent
            )
        }
    }
}
