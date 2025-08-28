package com.pardip.quizmaster.ui.quiz.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.pardip.quizmaster.R

@Composable
fun QuizImage(url: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val painter = if (LocalInspectionMode.current)
        painterResource(R.drawable.placeholder_image)
    else rememberAsyncImagePainter(url)

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.Crop
    )
}