package com.example.quizapp

import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.StringReader


class XmlQuizLoader(private val context: Context) {

    private val TAG = "XmlReadKotlin"

    suspend fun loadQuiz(): Quiz {
        // Sample XML quiz data - in a real app, this would come from assets or raw resources
        val xmlContent = readXmlAsString("geology1.xml")

        return parseXmlQuiz(xmlContent)
    }

    private fun readXmlAsString(fileName: String): String {
        var inputStream: InputStream? = null
        try {
            val assetManager = context.assets
            inputStream = assetManager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val xmlString = String(buffer)
            Log.d(TAG, "XML as String: \n$xmlString")
            return xmlString;
        } catch (e: Exception) {
            Log.e(TAG, "Error reading XML as String: ${e.message}", e)
        } finally {
            inputStream?.close()
        }
        return ""
    }

    private fun parseXmlQuiz(xmlContent: String): Quiz {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        var eventType = parser.eventType
        var quizTitle = ""
        val questions = mutableListOf<Question>()

        var currentQuestion = ""
        var currentOptions = mutableListOf<String>()
        var correctAnswer = 0
        var currentParserName = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "quiz" -> {
                            quizTitle = parser.getAttributeValue(null, "title") ?: "Quiz"
                        }
                        "question" -> {
                            currentQuestion = ""
                            currentOptions = mutableListOf()
                            correctAnswer = 0
                        }
                    }
                    currentParserName = parser.name
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    if (text.isNotEmpty()) {
                        when (currentParserName) {
                            "text" -> currentQuestion = text
                            "option" -> currentOptions.add(text)
                            "correct_answer" -> correctAnswer = text.toIntOrNull() ?: 0
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "question" -> {
                            questions.add(
                                Question(
                                    question = currentQuestion,
                                    options = currentOptions.toList(),
                                    correctAnswer = correctAnswer
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return Quiz(title = quizTitle, questions = questions)
    }
}
