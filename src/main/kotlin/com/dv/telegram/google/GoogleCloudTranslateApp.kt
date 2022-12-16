package com.dv.telegram.google

import com.google.cloud.translate.v3.LocationName
import com.google.cloud.translate.v3.TranslateTextRequest
import com.google.cloud.translate.v3.TranslationServiceClient
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException

/**
 * **!!!** You need to set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the path of your `key.json` file
 * containing the credentials valid to use the Google Cloud Translate API.
 */
object GoogleCloudTranslateApp : Logging {
    private val TEXT = """
            -- Для поиска информации по этой вики и получения ответов на ваши вопросы можно использовать 
            -- Телеграм-бота Дюсю
        """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) {
        // java.io.IOException: The Application Default Credentials are not available. They are available if running in Google Compute Engine.
        // Otherwise, the environment variable GOOGLE_APPLICATION_CREDENTIALS must be defined pointing to a file defining the credentials.
        // See https://developers.google.com/accounts/docs/application-default-credentials for more information.
        //	at com.google.auth.oauth2.DefaultCredentialsProvider.getDefaultCredentials(DefaultCredentialsProvider.java:134)
        try {
            translateText()
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    @Throws(IOException::class)
    fun translateText() {
        val projectId = "wiki-bot-project"
        val sourceLanguage = "ru"
        val targetLanguage = "uk" // see https://www.labnol.org/code/19899-google-translate-languages
        //        String targetLanguage = "de"; // see https://www.labnol.org/code/19899-google-translate-languages


//        String text = "Для поиска информации по этой вики и получения ответов на ваши вопросы можно использовать Телеграм-бота Дюсю.";

//        String text = "Чтобы добавлять информацию в эту вики, пишите и скидывайте ссылки в чат https://t.me/wiki_ukraine_links_chat.";
        val text = TEXT
        translateText(projectId, sourceLanguage, targetLanguage, text)
    }

    @Throws(IOException::class)
    fun translateText(projectId: String, sourceLanguage: String, targetLanguage: String, text: String): String {
        TranslationServiceClient.create().use { client ->
            val parent = LocationName.of(projectId, "global")
            val request = TranslateTextRequest
                .newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .setSourceLanguageCode(sourceLanguage)
                .setTargetLanguageCode(targetLanguage)
                .addContents(text)
                .build()

            val response = client.translateText(request)

            // Display the translation for each input text provided
            for (translation in response.translationsList) {
                logger.debug("Translated text [$sourceLanguage → $targetLanguage]: \n${translation.translatedText}")
            }

            // todo: maybe return multiple responses
            return response.translationsList[0].translatedText
        }
    }
}
