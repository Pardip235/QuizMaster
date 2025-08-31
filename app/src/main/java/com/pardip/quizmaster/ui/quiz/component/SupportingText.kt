package com.pardip.quizmaster.ui.quiz.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun SupportingText(
    text: String?,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
) {
    if (!text.isNullOrBlank()) {
        Text(
            text = text,
            color = color,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth()
        )
    }
}