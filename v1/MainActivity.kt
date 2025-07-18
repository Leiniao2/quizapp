// MainActivity.kt
package com.example.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.ui.theme.QuizAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuizApp()
                }
            }
        }
    }
}

// Data Models
data class Question(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val category: String
)

data class Quiz(
    val title: String,
    val questions: List<Question>
)

// Sample Data
object QuizData {
    val mathQuiz = Quiz(
        title = "Mathematics Quiz",
        questions = listOf(
            Question(
                id = 1,
                question = "What is 15 + 27?",
                options = listOf("40", "42", "45", "38"),
                correctAnswer = 1,
                category = "Math"
            ),
            Question(
                id = 2,
                question = "What is the square root of 64?",
                options = listOf("6", "7", "8", "9"),
                correctAnswer = 2,
                category = "Math"
            ),
            Question(
                id = 3,
                question = "What is 12 × 8?",
                options = listOf("84", "96", "104", "92"),
                correctAnswer = 1,
                category = "Math"
            )
        )
    )
    
    val scienceQuiz = Quiz(
        title = "Science Quiz",
        questions = listOf(
            Question(
                id = 4,
                question = "What is the chemical symbol for water?",
                options = listOf("H2O", "CO2", "O2", "H2SO4"),
                correctAnswer = 0,
                category = "Science"
            ),
            Question(
                id = 5,
                question = "How many bones are in the human body?",
                options = listOf("196", "206", "216", "226"),
                correctAnswer = 1,
                category = "Science"
            ),
            Question(
                id = 6,
                question = "What planet is closest to the Sun?",
                options = listOf("Venus", "Earth", "Mercury", "Mars"),
                correctAnswer = 2,
                category = "Science"
            )
        )
    )
    
    val historyQuiz = Quiz(
        title = "History Quiz",
        questions = listOf(
            Question(
                id = 7,
                question = "In which year did World War II end?",
                options = listOf("1944", "1945", "1946", "1947"),
                correctAnswer = 1,
                category = "History"
            ),
            Question(
                id = 8,
                question = "Who was the first President of the United States?",
                options = listOf("Thomas Jefferson", "John Adams", "George Washington", "Benjamin Franklin"),
                correctAnswer = 2,
                category = "History"
            ),
            Question(
                id = 9,
                question = "Which ancient wonder was located in Alexandria?",
                options = listOf("Hanging Gardens", "Lighthouse", "Colossus", "Mausoleum"),
                correctAnswer = 1,
                category = "History"
            )
        )
    )
    
    val allQuizzes = listOf(mathQuiz, scienceQuiz, historyQuiz)
}

// Quiz State Management
sealed class QuizState {
    object QuizSelection : QuizState()
    data class QuizInProgress(val quiz: Quiz, val currentQuestionIndex: Int) : QuizState()
    data class QuizCompleted(val quiz: Quiz, val score: Int, val totalQuestions: Int) : QuizState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizApp() {
    var quizState by remember { mutableStateOf<QuizState>(QuizState.QuizSelection) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    
    when (val state = quizState) {
        is QuizState.QuizSelection -> {
            QuizSelectionScreen(
                onQuizSelected = { quiz ->
                    selectedAnswers = mutableMapOf()
                    quizState = QuizState.QuizInProgress(quiz, 0)
                }
            )
        }
        
        is QuizState.QuizInProgress -> {
            QuizScreen(
                quiz = state.quiz,
                currentQuestionIndex = state.currentQuestionIndex,
                selectedAnswers = selectedAnswers,
                onAnswerSelected = { questionId, answerIndex ->
                    selectedAnswers = selectedAnswers.toMutableMap().apply {
                        this[questionId] = answerIndex
                    }
                },
                onNextQuestion = {
                    if (state.currentQuestionIndex < state.quiz.questions.size - 1) {
                        quizState = QuizState.QuizInProgress(state.quiz, state.currentQuestionIndex + 1)
                    } else {
                        val score = calculateScore(state.quiz, selectedAnswers)
                        quizState = QuizState.QuizCompleted(state.quiz, score, state.quiz.questions.size)
                    }
                },
                onPreviousQuestion = {
                    if (state.currentQuestionIndex > 0) {
                        quizState = QuizState.QuizInProgress(state.quiz, state.currentQuestionIndex - 1)
                    }
                }
            )
        }
        
        is QuizState.QuizCompleted -> {
            ResultScreen(
                quiz = state.quiz,
                score = state.score,
                totalQuestions = state.totalQuestions,
                selectedAnswers = selectedAnswers,
                onRetakeQuiz = {
                    selectedAnswers = mutableMapOf()
                    quizState = QuizState.QuizInProgress(state.quiz, 0)
                },
                onBackToSelection = {
                    quizState = QuizState.QuizSelection
                }
            )
        }
    }
}

@Composable
fun QuizSelectionScreen(onQuizSelected: (Quiz) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz App",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "Select a quiz to get started:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(QuizData.allQuizzes) { quiz ->
                QuizCard(
                    quiz = quiz,
                    onClick = { onQuizSelected(quiz) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizCard(quiz: Quiz, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${quiz.questions.size} questions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuizScreen(
    quiz: Quiz,
    currentQuestionIndex: Int,
    selectedAnswers: Map<Int, Int>,
    onAnswerSelected: (Int, Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit
) {
    val currentQuestion = quiz.questions[currentQuestionIndex]
    val selectedAnswer = selectedAnswers[currentQuestion.id]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentQuestionIndex + 1).toFloat() / quiz.questions.size },
            modifier = Modifier.fillMaxWidth(),
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question header
        Text(
            text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Question text
        Text(
            text = currentQuestion.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Answer options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            currentQuestion.options.forEachIndexed { index, option ->
                AnswerOption(
                    text = option,
                    isSelected = selectedAnswer == index,
                    onClick = {
                        onAnswerSelected(currentQuestion.id, index)
                    }
                )
            }
        }
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPreviousQuestion,
                enabled = currentQuestionIndex > 0
            ) {
                Text("Previous")
            }
            
            Button(
                onClick = onNextQuestion,
                enabled = selectedAnswer != null
            ) {
                Text(
                    if (currentQuestionIndex == quiz.questions.size - 1) "Finish" else "Next"
                )
            }
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ResultScreen(
    quiz: Quiz,
    score: Int,
    totalQuestions: Int,
    selectedAnswers: Map<Int, Int>,
    onRetakeQuiz: () -> Unit,
    onBackToSelection: () -> Unit
) {
    val percentage = (score.toFloat() / totalQuestions * 100).toInt()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Score display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    percentage >= 80 -> Color(0xFF4CAF50)
                    percentage >= 60 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = "$score/$totalQuestions",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }
        
        // Performance message
        Text(
            text = when {
                percentage >= 80 -> "Excellent work! 🎉"
                percentage >= 60 -> "Good job! 👍"
                else -> "Keep practicing! 💪"
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Review answers
        Text(
            text = "Review Answers:",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quiz.questions) { question ->
                QuestionReview(
                    question = question,
                    selectedAnswer = selectedAnswers[question.id],
                    isCorrect = selectedAnswers[question.id] == question.correctAnswer
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onBackToSelection,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Quizzes")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onRetakeQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retake Quiz")
            }
        }
    }
}

@Composable
fun QuestionReview(
    question: Question,
    selectedAnswer: Int?,
    isCorrect: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) 
                Color(0xFFE8F5E8) 
            else 
                Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your answer: ${selectedAnswer?.let { question.options[it] } ?: "Not answered"}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            
            if (!isCorrect) {
                Text(
                    text = "Correct answer: ${question.options[question.correctAnswer]}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

private fun calculateScore(quiz: Quiz, selectedAnswers: Map<Int, Int>): Int {
    return quiz.questions.count { question ->
        selectedAnswers[question.id] == question.correctAnswer
    }
}

@Preview(showBackground = true)
@Composable
fun QuizAppPreview() {
    QuizAppTheme {
        QuizApp()
    }
}
