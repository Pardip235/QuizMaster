package com.pardip.quizmaster.ui.quiz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pardip.quizmaster.ui.quiz.model.QuizUiState
import com.pardip.quizmaster.ui.quiz.model.UiOpenEnded
import com.pardip.quizmaster.ui.quiz.model.UiQuiz
import com.pardip.quizmaster.ui.quiz.model.UiSlider

@Composable
fun QuizScreen(
    state: QuizUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
           state.loading -> Text("Loading...")
           state.error != null -> Text("Error: ${state.error}")
           else -> QuizContent(state)

        }
    }
}

@Composable
fun QuizContent(state: QuizUiState) {
    when(val q = state.items.getOrNull(state.currentIndex)) {
        is UiOpenEnded -> {
            Column {
                Text("Open Ended Question: ${q.text}")
                Text("Your Answer: ${q.acceptedAnswers[state.currentIndex]}")
                // Add input field and submit button here
            }
        }
        is UiQuiz -> {
            Column {
                Text("Quiz Question: $q")

            }
        }
        is UiSlider -> Text("Slider Question: $q")
        null -> {

        }
    }
}
