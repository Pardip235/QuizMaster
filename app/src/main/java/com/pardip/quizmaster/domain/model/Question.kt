package com.pardip.quizmaster.domain.model

import kotlin.time.Duration

sealed interface Question {
    val text: String
    val imageUrl: String?
    val duration: Duration
    val altText: String?
}

data class MultipleChoice(
    override val text: String,
    override val imageUrl: String?,
    override val duration: Duration,
    override val altText: String?,
    val choices: List<Choice>
) : Question

data class OpenEnded(
    override val text: String,
    override val imageUrl: String?,
    override val duration: Duration,
    override val altText: String?,
    val acceptedAnswers: List<String>
) : Question

data class Slider(
    override val text: String,
    override val imageUrl: String?,
    override val duration: Duration,
    override val altText: String?,
    val start: Double,
    val end: Double,
    val step: Double,
    val correct: Double,
    val tolerance: Double
) : Question
