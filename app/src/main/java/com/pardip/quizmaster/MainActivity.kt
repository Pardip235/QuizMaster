package com.pardip.quizmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pardip.quizmaster.ui.theme.QuizMasterTheme
import com.pardip.quizmaster.ui.theme.QuizScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizMasterTheme {
                QuizScreen()
            }
        }
    }
}
