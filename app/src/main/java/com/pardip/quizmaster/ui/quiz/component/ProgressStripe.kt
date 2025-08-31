package com.pardip.quizmaster.ui.quiz.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.pardip.quizmaster.ui.theme.Purple

@Composable
fun ProgressStripe(
    progress: Float, // 0f..1f
    height: Dp,
    remainingSeconds: Int?, // label shown only if non-null
    barColor: Color = Purple,
    textColor: Color = Color.White,
    animationMillis: Int = 250,
    horizontalPadding: Dp = 8.dp
) {
    val fraction by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(animationMillis),
        label = "progress-anim"
    )

    val label = remainingSeconds?.toString().orEmpty()
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Measure label once per text/style so the bar never gets narrower than the label
    val labelWidthDp = remember(label) {
        if (label.isEmpty()){
            0.dp
        } else with(density) {
            measurer
                .measure(
                    text = AnnotatedString(label),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                .size.width.toDp()
        }
    }
    val minBarDp = if (label.isEmpty()) 0.dp else (labelWidthDp + horizontalPadding * 2)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp)) // capsule
    ) {
        val maxW = maxWidth
        val barW = remember(maxW, fraction, minBarDp) {
            max((maxW * fraction), minBarDp).coerceAtMost(maxW)
        }

        // Filled bar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(barW)
                .background(barColor, RoundedCornerShape(999.dp))
        ) {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = horizontalPadding)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProgressStripePreview() {
    ProgressStripe(
        progress = 1f,
        height = 26.dp,
        remainingSeconds = 26
    )
}
