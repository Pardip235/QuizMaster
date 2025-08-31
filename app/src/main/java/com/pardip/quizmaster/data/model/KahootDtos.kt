package com.pardip.quizmaster.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KahootResponse(
    val title: String,
    val cover: String,
    val questions: List<KahootQuestion>
)

@Serializable
data class KahootQuestion(
    val type: QuestionType,
    @SerialName("question")
    val text: String,
    val time: Int,
    val image: String? = null,
    val imageMetadata: ImageMetadata? = null,
    val media: List<KahootMedia>,
    val choices: List<KahootChoice> = emptyList(),
    val choiceRange: ChoiceRange? = null
)

@Serializable
enum class QuestionType {
    @SerialName("quiz") QUIZ,
    @SerialName("open_ended") OPEN_ENDED,
    @SerialName("slider") SLIDER
}

@Serializable
data class KahootChoice(
    @SerialName("answer") val text: String,
    @SerialName("correct") val isCorrect: Boolean
)

@Serializable
data class ChoiceRange(
    val start: Double,
    val end: Double,
    val step: Double,
    val correct: Double,
    val tolerance: Double
)

@Serializable
data class ImageMetadata(
    val altText: String? = null,
)

@Serializable
data class KahootMedia(
    val type: String? = null,
    val id: String? = null
)
