package com.pardip.quizmaster.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KahootResponse(
    val uuid: String,
    val title: String,
    val cover: String,
    val questions: List<KahootQuestion> = emptyList()
)

@Serializable
data class KahootQuestion(
    val type: QuestionType = QuestionType.UNKNOWN,
    @SerialName("question") val text: String? = null,
    val time: Int? = 20000,
    val image: String? = null,
    val imageMetadata: ImageMetadata? = null,
    val choices: List<KahootChoice> = emptyList(),
    val choiceRange: ChoiceRange? = null,
    val media: List<KahootMedia> = emptyList()
)

@Serializable
enum class QuestionType {
    @SerialName("quiz") QUIZ,
    @SerialName("open_ended") OPEN_ENDED,
    @SerialName("slider") SLIDER,
    UNKNOWN;
}

@Serializable
data class KahootChoice(
    @SerialName("answer") val text: String,
    @SerialName("correct") val isCorrect: Boolean = false
)

@Serializable
data class ChoiceRange(
    val start: Double,
    val end: Double,
    val step: Double,
    val correct: Double,
    val tolerance: Double = 0.0
)

@Serializable
data class ImageMetadata(
    val altText: String? = null,
)

@Serializable
data class KahootMedia(
    val type: String,
    val id: String,
)


