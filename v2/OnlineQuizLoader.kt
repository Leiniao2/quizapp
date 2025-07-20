package com.example.quizapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class OnlineQuizLoader {

    suspend fun loadQuiz(): Quiz = withContext(Dispatchers.IO) {
        // Simulate loading from an online API
        // In a real app, you'd make an HTTP request to your server

        // For demonstration, we'll simulate network delay and return a hardcoded quiz
        kotlinx.coroutines.delay(2000) // Simulate network delay

        // Sample JSON response that might come from your server
        val jsonResponse = """
            {
                "title": "Science Quiz",
                "questions": [
                    {
                        "question": "What is the chemical symbol for water?",
                        "options": ["H2O", "CO2", "NaCl", "O2"],
                        "correctAnswer": 0
                    },
                    {
                        "question": "How many bones are in the human body?",
                        "options": ["206", "208", "210", "212"],
                        "correctAnswer": 0
                    },
                    {
                        "question": "What is the speed of light?",
                        "options": ["299,792,458 m/s", "300,000,000 m/s", "250,000,000 m/s", "350,000,000 m/s"],
                        "correctAnswer": 0
                    },
                    {
                        "question": "Which gas makes up most of Earth's atmosphere?",
                        "options": ["Oxygen", "Nitrogen", "Carbon Dioxide", "Argon"],
                        "correctAnswer": 1
                    }
                ]
            }
        """.trimIndent()

        parseJsonQuiz(jsonResponse)
    }

    private fun parseJsonQuiz(jsonContent: String): Quiz {
        val jsonObject = JSONObject(jsonContent)
        val title = jsonObject.getString("title")
        val questionsArray = jsonObject.getJSONArray("questions")

        val questions = mutableListOf<Question>()
        for (i in 0 until questionsArray.length()) {
            val questionObject = questionsArray.getJSONObject(i)
            val questionText = questionObject.getString("question")
            val correctAnswer = questionObject.getInt("correctAnswer")

            val optionsArray = questionObject.getJSONArray("options")
            val options = mutableListOf<String>()
            for (j in 0 until optionsArray.length()) {
                options.add(optionsArray.getString(j))
            }

            questions.add(
                Question(
                    question = questionText,
                    options = options,
                    correctAnswer = correctAnswer
                )
            )
        }

        return Quiz(title = title, questions = questions)
    }
}
