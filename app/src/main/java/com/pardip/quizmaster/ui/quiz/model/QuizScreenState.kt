package com.pardip.quizmaster.ui.quiz.model

enum class RevealReason { CORRECT, WRONG, TIME_UP }

data class QuizUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val title: String = "",
    val items: List<UiQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val progress: Float = 1f,
    val showSolution: Boolean = false,
    val reveal: RevealReason? = null,
    val inlineMessage: String? = null,
    val remainingSeconds: Int? = null,

    // Per-type UI state
    val selectedIndex: Int? = null,       // UiQuiz
    val typedAnswer: String = "",         // UiOpenEnded
    val sliderValue: Int? = null,         // UiSlider

    val finished: Boolean = false
)