package com.pardip.quizmaster.ui.quiz.model

sealed interface UiQuestion {
    val text: String
    val imageUrl: String?
    val timeMs: Int
    val layout: String?
    val altText: String?
}

data class UiQuiz(
    override val text: String,
    override val imageUrl: String?,
    override val timeMs: Int,
    override val layout: String?,
    override val altText: String?,
    val answers: List<String>,
    val correctIndices: Set<Int>
) : UiQuestion

data class UiOpenEnded(
    override val text: String,
    override val imageUrl: String?,
    override val timeMs: Int,
    override val layout: String?,
    override val altText: String?,
    val acceptedAnswers: List<String>
) : UiQuestion

data class UiSlider(
    override val text: String,
    override val imageUrl: String?,
    override val timeMs: Int,
    override val layout: String?,
    override val altText: String?,
    val start: Double,
    val end: Double,
    val step: Double,
    val correct: Double,
    val tolerance: Double
) : UiQuestion



