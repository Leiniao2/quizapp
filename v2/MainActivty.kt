// MainActivity.kt
@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun QuizApp() {
    val viewModel: QuizViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.currentScreen) {
        Screen.MENU -> MenuScreen(
            onLoadXmlQuiz = { viewModel.loadXmlQuiz() },
            onLoadOnlineQuiz = { viewModel.loadOnlineQuiz() }
        )
        Screen.LOADING -> LoadingScreen()
        Screen.QUIZ -> QuizScreen(
            quiz = uiState.currentQuiz!!,
            currentQuestionIndex = uiState.currentQuestionIndex,
            selectedAnswer = uiState.selectedAnswer,
            onAnswerSelected = { viewModel.selectAnswer(it) },
            onNextQuestion = { viewModel.nextQuestion() },
            onBackToMenu = { viewModel.backToMenu() }
        )
        Screen.RESULT -> ResultScreen(
            score = uiState.score,
            totalQuestions = uiState.currentQuiz?.questions?.size ?: 0,
            onRestartQuiz = { viewModel.restartQuiz() },
            onBackToMenu = { viewModel.backToMenu() }
        )
        Screen.ERROR -> ErrorScreen(
            message = uiState.errorMessage,
            onRetry = { viewModel.backToMenu() }
        )
    }
}

@Composable
fun MenuScreen(
    onLoadXmlQuiz: () -> Unit,
    onLoadOnlineQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "刷题宝",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onLoadXmlQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Load Quiz from XML", fontSize = 18.sp)
        }

        Button(
            onClick = onLoadOnlineQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Load Quiz from Server", fontSize = 18.sp)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading Quiz...")
        }
    }
}

@Composable
fun QuizScreen(
    quiz: Quiz,
    currentQuestionIndex: Int,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val currentQuestion = quiz.questions[currentQuestionIndex]
    val isLastQuestion = currentQuestionIndex == quiz.questions.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = (currentQuestionIndex + 1).toFloat() / quiz.questions.size,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question counter
        Text(
            text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question text
        Text(
            text = currentQuestion.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Answer options
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentQuestion.options.size) { index ->
                AnswerOption(
                    text = currentQuestion.options[index],
                    isSelected = selectedAnswer == index,
                    onClick = { onAnswerSelected(index) }
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBackToMenu) {
                Text("Back to Menu")
            }

            Button(
                onClick = onNextQuestion,
                enabled = selectedAnswer != null
            ) {
                Text(if (isLastQuestion) "Finish Quiz" else "Next Question")
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    onRestartQuiz: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val percentage = (score.toFloat() / totalQuestions * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Complete!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Your Score",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "$score / $totalQuestions",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "$percentage%",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onRestartQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Restart Quiz", fontSize = 18.sp)
        }

        OutlinedButton(
            onClick = onBackToMenu,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Back to Menu", fontSize = 18.sp)
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = message,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(onClick = onRetry) {
            Text("Back to Menu")
        }
    }
}
