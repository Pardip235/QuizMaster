package com.pardip.quizmaster.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pardip.quizmaster.data.repository.KahootRepository
import com.pardip.quizmaster.data.util.NetworkResult
import com.pardip.quizmaster.ui.quiz.mapper.toUi
import com.pardip.quizmaster.ui.quiz.model.QuizUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val repo: KahootRepository = KahootRepository(),
) : ViewModel() {

    private val _ui = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _ui

    init {
        load()
    }

    private fun load() {
        _ui.value = QuizUiState(loading = true)
        viewModelScope.launch {
            when (val r = repo.load(QuizConfig.DEFAULT_ID)) {
                is NetworkResult.Success -> {
                    val items = r.data.toUi()
                    _ui.value =
                        if (items.isEmpty()) QuizUiState(
                            loading = false,
                            error = "No playable questions"
                        )
                        else QuizUiState(
                            loading = false,
                            title = r.data.title ?: "Quiz",
                            items = items
                        )
                }

                is NetworkResult.Error -> {
                    val msg = r.message + (r.code?.let { " (HTTP $it)" } ?: "")
                    _ui.value = QuizUiState(loading = false, error = msg)
                }
            }
        }
    }

    object QuizConfig {
        const val DEFAULT_ID = "fb4054fc-6a71-463e-88cd-243876715bc1"
    }
}