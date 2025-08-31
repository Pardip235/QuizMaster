package com.pardip.quizmaster.data.converter

import com.pardip.quizmaster.data.model.KahootResponse
import com.pardip.quizmaster.data.model.QuestionType
import com.pardip.quizmaster.domain.model.Choice
import com.pardip.quizmaster.domain.model.OpenEnded
import com.pardip.quizmaster.domain.model.Question
import com.pardip.quizmaster.domain.model.MultipleChoice
import com.pardip.quizmaster.domain.model.Slider
import kotlin.time.Duration.Companion.milliseconds

/**
 * Maps a Kahoot DTO to domain models.
 */
fun KahootResponse.asDomainModel(): List<Question> {
    val coverUrl = cover
    return questions.mapNotNull { q ->
        val text = q.text.trim()
        if (text.isEmpty()) return@mapNotNull null

        val imageUrl = q.image
            ?: q.media.firstOrNull { it.type == "background_image" }?.id
            ?: coverUrl

        val duration = q.time.milliseconds
        val alt = q.imageMetadata?.altText

        when (q.type) {
            QuestionType.QUIZ -> {
                val choices = q.choices
                    .map { Choice(text = it.text, isCorrect = it.isCorrect) }
                    .filter { it.text.isNotBlank() }
                if (choices.isEmpty()) return@mapNotNull null
                MultipleChoice(
                    text = text,
                    imageUrl = imageUrl,
                    duration = duration,
                    altText = alt,
                    choices = choices
                )
            }
            QuestionType.OPEN_ENDED -> {
                val accepted = q.choices
                    .filter { it.isCorrect }
                    .map { it.text }
                    .filter { it.isNotBlank() }
                if (accepted.isEmpty()) return@mapNotNull null
                OpenEnded(
                    text = text,
                    imageUrl = imageUrl,
                    duration = duration,
                    altText = alt,
                    acceptedAnswers = accepted
                )
            }
            QuestionType.SLIDER -> {
                val r = q.choiceRange ?: return@mapNotNull null
                Slider(
                    text = text,
                    imageUrl = imageUrl,
                    duration = duration,
                    altText = alt,
                    start = r.start,
                    end = r.end,
                    step = if (r.step > 0.0) r.step else 1.0,
                    correct = r.correct,
                    tolerance = r.tolerance.coerceAtLeast(0.0)
                )
            }
        }
    }
}