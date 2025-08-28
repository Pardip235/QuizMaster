package com.pardip.quizmaster.ui.quiz.model

import com.pardip.quizmaster.data.util.ErrorType

enum class RevealReason { CORRECT, WRONG, TIME_UP }

data class QuizUiState(
    val loading: Boolean = true,
    val error: ErrorType? = null,
    val title: String = "",
    val items: List<UiQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val progress: Float = 1f,
    val remainingSeconds: Int? = null,
    val showSolution: Boolean = false,
    val reveal: RevealReason? = null,

    // UI-only banners (strings resolved by UI)
    val showTimeUpBanner: Boolean = false,
    val showNoAnswerBanner: Boolean = false,

    // Per-type state
    val selectedIndex: Int? = null,   // UiQuiz
    val typedAnswer: String = "",     // UiOpenEnded
    val sliderValue: Int? = null,     // UiSlider

    // End state
    val finished: Boolean = false,

    // Content issue (not network): no playable questions in payload
    val noPlayableQuestions: Boolean = false
)