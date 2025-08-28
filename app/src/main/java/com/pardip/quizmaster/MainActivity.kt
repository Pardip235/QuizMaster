package com.pardip.quizmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.pardip.quizmaster.ui.quiz.QuizScreen
import com.pardip.quizmaster.ui.quiz.QuizViewModel
import com.pardip.quizmaster.ui.theme.QuizMasterTheme

class MainActivity : ComponentActivity() {
    private val viewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizMasterTheme {
                QuizScreen(viewModel = viewModel)
            }
        }
    }
}
