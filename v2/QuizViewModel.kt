package com.example.quizapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val xmlQuizLoader = XmlQuizLoader(application)
    private val onlineQuizLoader = OnlineQuizLoader()

    fun loadXmlQuiz() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentScreen = Screen.LOADING)
            try {
                val quiz = xmlQuizLoader.loadQuiz()
                _uiState.value = _uiState.value.copy(
                    currentScreen = Screen.QUIZ,
                    currentQuiz = quiz,
                    currentQuestionIndex = 0,
                    selectedAnswer = null,
                    score = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    currentScreen = Screen.ERROR,
                    errorMessage = "Failed to load XML quiz: ${e.message}"
                )
            }
        }
    }

    fun loadOnlineQuiz() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentScreen = Screen.LOADING)
            try {
                val quiz = onlineQuizLoader.loadQuiz()
                _uiState.value = _uiState.value.copy(
                    currentScreen = Screen.QUIZ,
                    currentQuiz = quiz,
                    currentQuestionIndex = 0,
                    selectedAnswer = null,
                    score = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    currentScreen = Screen.ERROR,
                    errorMessage = "Failed to load online quiz: ${e.message}"
                )
            }
        }
    }

    fun selectAnswer(answerIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedAnswer = answerIndex)
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val currentQuiz = currentState.currentQuiz ?: return
        val currentQuestion = currentQuiz.questions[currentState.currentQuestionIndex]

        // Check if answer is correct
        val isCorrect = currentState.selectedAnswer == currentQuestion.correctAnswer
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        if (currentState.currentQuestionIndex < currentQuiz.questions.size - 1) {
            // Move to next question
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1,
                selectedAnswer = null,
                score = newScore
            )
        } else {
            // Quiz finished
            _uiState.value = currentState.copy(
                currentScreen = Screen.RESULT,
                score = newScore
            )
        }
    }

    fun restartQuiz() {
        val currentQuiz = _uiState.value.currentQuiz ?: return
        _uiState.value = _uiState.value.copy(
            currentScreen = Screen.QUIZ,
            currentQuestionIndex = 0,
            selectedAnswer = null,
            score = 0
        )
    }

    fun backToMenu() {
        _uiState.value = QuizUiState()
    }
}

// Data Models
data class Quiz(
    val title: String,
    val questions: List<Question>
)

data class Question(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

data class QuizUiState(
    val currentScreen: Screen = Screen.MENU,
    val currentQuiz: Quiz? = null,
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val score: Int = 0,
    val errorMessage: String = ""
)

enum class Screen {
    MENU, LOADING, QUIZ, RESULT, ERROR
}
