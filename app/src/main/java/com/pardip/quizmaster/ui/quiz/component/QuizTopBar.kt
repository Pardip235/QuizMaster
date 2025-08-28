package com.pardip.quizmaster.ui.quiz.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pardip.quizmaster.R
import com.pardip.quizmaster.ui.quiz.model.RevealReason
import com.pardip.quizmaster.ui.theme.CorrectColor
import com.pardip.quizmaster.ui.theme.WrongColor


@Composable
fun QuizTopBar(
    reveal: RevealReason?,
    index: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    when (reveal) {
        RevealReason.CORRECT -> ResultBanner(
            text = stringResource(R.string.correct),
            color = CorrectColor,
            modifier = modifier
        )

        RevealReason.WRONG, RevealReason.TIME_UP -> ResultBanner(
            text = stringResource(R.string.wrong),
            color = WrongColor, modifier = modifier
        )

        else -> QuizHeader(
            index = index,
            total = total,
            modifier = modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        )
    }
}

@Preview
@Composable
private fun QuizTopBarPreview() {
    QuizTopBar(
        reveal = RevealReason.CORRECT,
        index = 2,
        total = 12
    )
}