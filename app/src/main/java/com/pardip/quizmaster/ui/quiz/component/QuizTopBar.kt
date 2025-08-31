package com.pardip.quizmaster.ui.quiz.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pardip.quizmaster.R
import com.pardip.quizmaster.ui.quiz.QuizScreenState
import com.pardip.quizmaster.ui.theme.CorrectColor
import com.pardip.quizmaster.ui.theme.WrongColor


@Composable
fun QuizTopBar(
    contentState: QuizScreenState.Content,
) {
    when (contentState.currentQuestionState) {
        is QuizScreenState.Content.QuestionState.Answered.Correct -> {
            ResultBanner(
                text = stringResource(R.string.correct),
                color = CorrectColor,
            )
        }

        is QuizScreenState.Content.QuestionState.TimesUp,
        is QuizScreenState.Content.QuestionState.Answered.Wrong -> {
            ResultBanner(
                text = stringResource(R.string.wrong),
                color = WrongColor,
            )
        }

        is QuizScreenState.Content.QuestionState.Answered.InputNumber,
        is QuizScreenState.Content.QuestionState.Answered.InputText,
        is QuizScreenState.Content.QuestionState.Progress,
        is QuizScreenState.Content.QuestionState.End -> {
            QuizHeader(
                index = contentState.currentIndex,
                total = contentState.totalQuestions,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )
        }
    }
}