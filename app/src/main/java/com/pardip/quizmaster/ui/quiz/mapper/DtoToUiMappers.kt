package com.pardip.quizmaster.ui.quiz.mapper

import com.pardip.quizmaster.data.remote.dto.KahootResponse
import com.pardip.quizmaster.data.remote.dto.QuestionType
import com.pardip.quizmaster.ui.quiz.model.UiOpenEnded
import com.pardip.quizmaster.ui.quiz.model.UiQuestion
import com.pardip.quizmaster.ui.quiz.model.UiQuiz
import com.pardip.quizmaster.ui.quiz.model.UiSlider

fun KahootResponse.toUi(): List<UiQuestion> {
    val coverUrl = cover
    return questions.mapNotNull { q ->
        val text = q.text?.trim().orEmpty()
        if (text.isEmpty()) return@mapNotNull null

        val imageUrl =
            q.image ?: q.media.firstOrNull { it.type == "background_image" }?.id ?: coverUrl
        val time = (q.time ?: 20_000).coerceIn(5_000, 120_000)
        val alt = q.imageMetadata?.altText

        when (q.type) {
            QuestionType.QUIZ -> {
                val answers = q.choices.map { it.text }.filter { it.isNotBlank() }
                if (answers.isEmpty()) return@mapNotNull null
                val correct =
                    q.choices.mapIndexedNotNull { i, c -> if (c.isCorrect) i else null }.toSet()
                UiQuiz(
                    text = text,
                    imageUrl = imageUrl,
                    timeMs = time,
                    layout = q.layout?.name,
                    altText = alt,
                    answers = answers,
                    correctIndices = correct
                )
            }

            QuestionType.OPEN_ENDED -> {
                val accepted =
                    q.choices.filter { it.isCorrect }.map { it.text }.filter { it.isNotBlank() }
                if (accepted.isEmpty()) return@mapNotNull null
                UiOpenEnded(
                    text = text,
                    imageUrl = imageUrl,
                    timeMs = time,
                    layout = q.layout?.name,
                    altText = alt,
                    acceptedAnswers = accepted
                )
            }

            QuestionType.SLIDER -> {
                val ints = q.choiceRange ?: return@mapNotNull null
                UiSlider(
                    text = text,
                    imageUrl = imageUrl,
                    timeMs = time,
                    layout = q.layout?.name,
                    altText = alt,
                    start = ints.start,
                    end = ints.end,
                    step = ints.step,
                    correct = ints.correct,
                    tolerance = ints.tolerance
                )
            }

            else -> null
        }
    }
}
