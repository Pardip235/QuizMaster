package com.pardip.quizmaster.ui.quiz.model

data class QuizUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val title: String = "",
    val items: List<UiQuestion> = emptyList(),
    val currentIndex: Int = 0,
)