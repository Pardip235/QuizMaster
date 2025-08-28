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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.pardip.quizmaster.ui.quiz.model.RevealReason

@Composable
fun QuizCardScaffold(
    reveal: RevealReason?,
    index: Int,
    total: Int,
    imageUrl: String?,
    questionText: String,
    answersCount: Int,
    answersContent: @Composable (Dp) -> Unit,
    supportingText: String?,
    showProgress: Boolean,
    progress: Float,
    remainingSeconds: Int?,
    primaryActionLabel: String?,
    onPrimaryAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuizTopBar(reveal = reveal, index = index, total = total, modifier = modifier)

        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {

            if (!imageUrl.isNullOrBlank()) {
                QuizImage(
                    url = imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
            }

            QuestionCard(
                text = questionText,
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
                // Compute a per-card height from the space given to this block
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val rows = ((answersCount + 1) / 2).coerceAtLeast(1)
                    val vGap = 8.dp
                    val minCard = 56.dp
                    val maxCard = 120.dp

                    // Height available for each row after subtracting gaps
                    val cardHeight = ((maxHeight - (rows - 1) * vGap) / rows)
                        .coerceIn(minCard, maxCard)

                    answersContent(cardHeight)
                }
            }

            if (!supportingText.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                SupportingText(
                    text = supportingText,
                    modifier = Modifier
                )
            }

            if (showProgress) {
                Spacer(Modifier.height(8.dp))
                ProgressStripe(
                    progress = progress,
                    remainingSeconds = remainingSeconds,
                    height = 24.dp,
                )
            }

            if (primaryActionLabel != null && onPrimaryAction != null) {
                Spacer(Modifier.height(8.dp))
                SimpleRoundedButton(
                    label = primaryActionLabel,
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .height(48.dp)
                        .width(200.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

