package com.pardip.quizmaster.ui.quiz.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun ProgressStripe(
    progress: Float,                 // 0f..1f
    height: Dp,
    remainingSeconds: Int?,          // show label only if non-null
    barColor: Color = Color(0xFF9C27B0),
    textColor: Color = Color.White,
    animationMillis: Int = 250,
    horizontalPadding: Dp = 8.dp
) {
    // Smoothly animate progress updates from VM
    val fraction by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(animationMillis),
        label = "progress-anim"
    )

    // We’ll measure the label so the bar never shrinks under it
    val textMeasurer = rememberTextMeasurer()
    val label = remainingSeconds?.toString() ?: ""

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp)) // capsule
    ) {
        val density = LocalDensity.current
        val fullWidthPx = with(density) { maxWidth.toPx() }
        val fullHeightPx = with(density) { height.toPx() }
        val padPx = with(density) { horizontalPadding.toPx() }

        // Measure the label (only once per text + font)
        val labelWidthPx = if (label.isNotEmpty()) {
            textMeasurer.measure(
                text = AnnotatedString(label),
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            ).size.width.toFloat()
        } else 0f

        // Base bar width from progress
        val baseBarPx = (fraction * fullWidthPx)
        // Ensure bar is never smaller than label + inner padding
        val minForLabelPx = if (labelWidthPx > 0f) labelWidthPx + padPx * 2f else 0f
        val barPx = max(baseBarPx, minForLabelPx).coerceAtMost(fullWidthPx)
        val barDp = with(density) { barPx.toDp() }

        // Draw the filled bar behind everything (no background track)
        Canvas(Modifier.matchParentSize()) {
            if (barPx > 0f) {
                drawRoundRect(
                    color = barColor,
                    size = Size(width = barPx, height = fullHeightPx),
                    cornerRadius = CornerRadius(fullHeightPx / 2f, fullHeightPx / 2f)
                )
            }
        }

        // Label rides the trailing edge (always visible because barPx ≥ label+padding)
        if (label.isNotEmpty() && barPx > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(barDp),                // container is exactly bar width
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = horizontalPadding),
                    maxLines = 1,
                    softWrap = false
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
