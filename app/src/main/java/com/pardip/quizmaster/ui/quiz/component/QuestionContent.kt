package com.pardip.quizmaster.ui.quiz.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.pardip.quizmaster.domain.model.Question

@Composable
fun QuestionContent(
    question: Question,
    answersCount: Int,
    answersContent: @Composable (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            question.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                QuizImage(
                    url = imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio = 16 / 9f),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
            }

            QuestionCard(
                text = question.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f, fill = true)
            ) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val rows = ((answersCount + 1) / 2).coerceAtLeast(1)
                    val vGap = 8.dp
                    val minCard = 56.dp
                    val maxCard = 120.dp
                    val cardHeight = ((maxHeight - (rows - 1) * vGap) / rows)
                        .coerceIn(minCard, maxCard)
                    answersContent(cardHeight)
                }
            }
        }
    }
}