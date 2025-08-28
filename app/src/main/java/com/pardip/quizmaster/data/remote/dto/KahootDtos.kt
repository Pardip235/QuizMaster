package com.pardip.quizmaster.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class KahootResponse(
    val uuid: String? = null,
    val title: String? = null,
    val description: String? = null,
    val cover: String? = null,
    val questions: List<KahootQuestion> = emptyList()
)

@Serializable
data class KahootQuestion(
    val type: QuestionType = QuestionType.UNKNOWN,
    @SerialName("question") val text: String? = null,
    val time: Int? = 20000,
    val points: Boolean? = null,
    val pointsMultiplier: Int? = null,
    val layout: LayoutType? = null,
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
    @SerialName("true_false") TRUE_FALSE, // if server uses this sometimes
    UNKNOWN;
}


@Serializable
enum class LayoutType {
    @SerialName("TRUE_FALSE") TRUE_FALSE,
    @SerialName("DEFAULT") DEFAULT,
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
    val id: String? = null,
    val altText: String? = null,
    val contentType: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class KahootMedia(
    val type: String,     // e.g. "background_image"
    val id: String? = null,
    val altText: String? = null,
    val contentType: String? = null,
    val width: Int? = null,
    val height: Int? = null
)


