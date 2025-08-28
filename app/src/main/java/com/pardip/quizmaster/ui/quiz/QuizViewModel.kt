package com.pardip.quizmaster.ui.quiz

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pardip.quizmaster.data.repository.KahootRepository
import com.pardip.quizmaster.data.util.NetworkResult
import com.pardip.quizmaster.ui.quiz.logic.countdownFlow
import com.pardip.quizmaster.ui.quiz.logic.normalizeAnswer
import com.pardip.quizmaster.ui.quiz.logic.snapToStep
import com.pardip.quizmaster.ui.quiz.mapper.toUi
import com.pardip.quizmaster.ui.quiz.model.QuizUiState
import com.pardip.quizmaster.ui.quiz.model.RevealReason
import com.pardip.quizmaster.ui.quiz.model.UiOpenEnded
import com.pardip.quizmaster.ui.quiz.model.UiQuestion
import com.pardip.quizmaster.ui.quiz.model.UiQuiz
import com.pardip.quizmaster.ui.quiz.model.UiSlider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

class QuizViewModel(
    private val repo: KahootRepository = KahootRepository(),
) : ViewModel() {

    private val _ui = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _ui

    private var timerJob: Job? = null

    init {
        load()
    }

    fun load(id: String = QuizConfig.DEFAULT_ID) {
        timerJob?.cancel()
        _ui.value = QuizUiState(loading = true)

        viewModelScope.launch {
            when (val r = repo.load(id)) {
                is NetworkResult.Success -> {
                    val data = r.data
                    val items = data.toUi()
                    if (items.isEmpty()) {
                        _ui.value = QuizUiState(
                            loading = false,
                            noPlayableQuestions = true
                        )
                        return@launch
                    }
                    _ui.value = QuizUiState(
                        loading = false,
                        title = data.title,
                        items = items
                    )
                    startTimer()
                }
                is NetworkResult.Error -> {
                    _ui.value = QuizUiState(
                        loading = false,
                        error = r.type
                    )
                }
            }
        }
    }

    private fun current(): UiQuestion? =
        _ui.value.items.getOrNull(_ui.value.currentIndex)

    private fun startTimer() {
        timerJob?.cancel()

        val q = current() ?: return
        val total = max(5_000, q.timeMs).milliseconds
        val endAt = SystemClock.elapsedRealtime() + total.inWholeMilliseconds

        _ui.value = _ui.value.copy(
            progress = 1f,
            remainingSeconds = ceil(total.inWholeMilliseconds / 1000.0).toInt(),
            showSolution = false,
            reveal = null,
            showTimeUpBanner = false,
            showNoAnswerBanner = false,
            selectedIndex = null,
            typedAnswer = "",
            sliderValue = (q as? UiSlider)?.start?.toInt()
        )

        timerJob = viewModelScope.launch {
            countdownFlow(total).collect { remainingMs ->
                val newProgress = remainingMs / total.inWholeMilliseconds.toFloat()
                val newSecs = ceil(remainingMs / 1000.0).toInt()

                _ui.update { s ->
                    if (s.progress == newProgress && s.remainingSeconds == newSecs) s
                    else s.copy(progress = newProgress, remainingSeconds = newSecs)
                }

                if (remainingMs == 0L) {
                    _ui.update {
                        it.copy(
                            showTimeUpBanner = true,
                            showSolution = true,
                            reveal = RevealReason.TIME_UP
                        )
                    }
                }
            }
        }
    }

    fun selectAnswer(index: Int) {
        val q = current() as? UiQuiz ?: return
        if (_ui.value.showSolution) return
        val correct = index in q.correctIndices
        timerJob?.cancel()
        _ui.value = _ui.value.copy(
            selectedIndex = index,
            showSolution = true,
            reveal = if (correct) RevealReason.CORRECT else RevealReason.WRONG
        )
    }

    fun typeOpenEnded(value: String) {
        if (_ui.value.showSolution) return
        _ui.value = _ui.value.copy(typedAnswer = value)
    }

    fun submitOpenEnded() {
        val q = current() as? UiOpenEnded ?: return
        if (_ui.value.showSolution) return
        val user = normalizeAnswer(_ui.value.typedAnswer)
        val correct = q.acceptedAnswers.any { normalizeAnswer(it) == user }
        timerJob?.cancel()
        _ui.value = _ui.value.copy(
            showSolution = true,
            reveal = if (correct) RevealReason.CORRECT else RevealReason.WRONG,
            showNoAnswerBanner = _ui.value.typedAnswer.isBlank()
        )
    }

    fun setSlider(value: Int) {
        val q = current() as? UiSlider ?: return
        if (_ui.value.showSolution) return
        val clamped = value.coerceIn(q.start.toInt(), q.end.toInt())
        val snapped = snapToStep(clamped, q.start.toInt(), maxOf(q.step.toInt(), 1))
        _ui.value = _ui.value.copy(sliderValue = snapped)
    }

    fun submitSlider() {
        val q = current() as? UiSlider ?: return
        if (_ui.value.showSolution) return
        val v = _ui.value.sliderValue ?: q.start.toInt()
        val correct = abs(v - q.correct) <= q.tolerance
        timerJob?.cancel()
        _ui.value = _ui.value.copy(
            showSolution = true,
            reveal = if (correct) RevealReason.CORRECT else RevealReason.WRONG
        )
    }

    fun continueNext() {
        val s = _ui.value
        if (!s.showSolution) return
        timerJob?.cancel()

        val next = s.currentIndex + 1
        if (next >= s.items.size) {
            _ui.value = s.copy(
                finished = true,
                showSolution = false,
                reveal = null,
                showTimeUpBanner = false,
                showNoAnswerBanner = false
            )
        } else {
            _ui.value = s.copy(
                currentIndex = next,
                showSolution = false,
                reveal = null,
                showTimeUpBanner = false,
                showNoAnswerBanner = false,
                progress = 1f,
                selectedIndex = null,
                typedAnswer = "",
                sliderValue = null
            )
            startTimer()
        }
    }

    object QuizConfig {
        const val DEFAULT_ID = "fb4054fc-6a71-463e-88cd-243876715bc1"
    }
}