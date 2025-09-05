package com.pardip.quizmaster.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pardip.quizmaster.core.net.ErrorType
import com.pardip.quizmaster.core.net.NetworkResult
import com.pardip.quizmaster.core.util.QuizConfig
import com.pardip.quizmaster.domain.model.MultipleChoice
import com.pardip.quizmaster.domain.model.OpenEnded
import com.pardip.quizmaster.domain.model.Question
import com.pardip.quizmaster.domain.model.Slider
import com.pardip.quizmaster.domain.usecase.GetKahootUseCase
import com.pardip.quizmaster.ui.quiz.util.clamp
import com.pardip.quizmaster.ui.quiz.util.normalizeAnswer
import com.pardip.quizmaster.ui.quiz.util.snapToStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

private const val AUTO_ADVANCE_DELAY_MS = 2_000L

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val getKahoot: GetKahootUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow<QuizScreenState>(QuizScreenState.Loading)
    val uiState: StateFlow<QuizScreenState> = _ui

    // hold items for index navigation
    private var items: List<Question> = emptyList()
    private var index = 0

    private var autoJob: Job? = null

    init {
        load()
    }

    fun load() {
        _ui.value = QuizScreenState.Loading
        viewModelScope.launch {
            when (val result = getKahoot.invoke(id = QuizConfig.DEFAULT_ID)) {
                is NetworkResult.Success -> {
                    items = result.data
                    index = 0
                    if (items.isEmpty()) {
                        _ui.value = QuizScreenState.Error(ErrorType.Unexpected) // or NoQuestions
                        return@launch
                    }
                    _ui.value = QuizScreenState.Content(
                        currentIndex = index,
                        totalQuestions = items.size,
                        question = items[index],
                        currentQuestionState = QuizScreenState.Content.QuestionState.Progress
                    )
                }

                is NetworkResult.Error -> _ui.value = QuizScreenState.Error(result.type)
            }
        }
    }

    fun onTimeUp() {
        val s = _ui.value as? QuizScreenState.Content ?: return
        // Don’t override if solution already shown
        if (s.currentQuestionState !is QuizScreenState.Content.QuestionState.Progress) return

        val correctIdx = when (val q = s.question) {
            is MultipleChoice -> q.choices
                .indexOfFirst { it.isCorrect }
                .let { if (it >= 0) it else -1 }

            else -> -1 // OpenEnded/Slider don’t have a single “correct index”
        }

        setQuestionState(QuizScreenState.Content.QuestionState.TimesUp(correctIdx))
    }


    fun submitMultipleChoice(index: Int) {
        val s = _ui.value as? QuizScreenState.Content ?: return
        val q = s.question as? MultipleChoice ?: return
        if (s.currentQuestionState !is QuizScreenState.Content.QuestionState.Progress) return

        val correct = q.choices.getOrNull(index)?.isCorrect == true

        val netState = if (correct) {
            QuizScreenState.Content.QuestionState.Answered.Correct(selectedIndex = index)
        } else {
            QuizScreenState.Content.QuestionState.Answered.Wrong(
                selectedIndex = index,
                correctIndex = q.choices
                    .indexOfFirst { it.isCorrect }
                    .coerceAtLeast(-1)
            )
        }
        setQuestionState(newState = netState)
    }

    fun submitOpenEnded(text: String) {
        val s = (_ui.value as? QuizScreenState.Content) ?: return
        val q = (s.question as? OpenEnded) ?: return
        val norm = normalizeAnswer(text)
        val ok = q.acceptedAnswers.any { normalizeAnswer(it) == norm }

        val newState = if (ok) {
            QuizScreenState.Content.QuestionState.Answered.Correct(selectedIndex = -1)
        } else {
            QuizScreenState.Content.QuestionState.Answered.Wrong(
                selectedIndex = -1, correctIndex = -1
            )
        }

        setQuestionState(newState = newState)
    }

    fun submitSlider(submitted: Double) {
        val contentState = _ui.value as? QuizScreenState.Content ?: return
        val q = contentState.question as? Slider ?: return

        // Normalize what came from UI (idempotent if UI already normalized)
        val clamped = clamp(submitted, q.start, q.end)
        val snapped = snapToStep(clamped, q.start, q.step)
        val valid = abs(snapped - q.correct) <= q.tolerance

        val newState = if (valid) {
            QuizScreenState.Content.QuestionState.Answered.Correct(
                selectedIndex = snapped.roundToInt()
            )
        } else {
            QuizScreenState.Content.QuestionState.Answered.Wrong(
                selectedIndex = snapped.roundToInt(),
                correctIndex = q.correct.roundToInt()
            )
        }

        setQuestionState(newState = newState)
    }

    fun continueNext() {
        autoJob?.cancel()
        goToNext()
    }

    private fun setQuestionState(newState: QuizScreenState.Content.QuestionState) {
        val s = _ui.value as? QuizScreenState.Content ?: return
        if (s.currentQuestionState == newState) return
        _ui.value = s.copy(currentQuestionState = newState)
        scheduleAutoAdvanceIfNeeded()
    }

    private fun scheduleAutoAdvanceIfNeeded() {
        autoJob?.cancel()
        val s = _ui.value as? QuizScreenState.Content ?: return
        when (s.currentQuestionState) {
            QuizScreenState.Content.QuestionState.Progress,
            QuizScreenState.Content.QuestionState.End -> return

            else -> {
                autoJob = viewModelScope.launch {
                    delay(AUTO_ADVANCE_DELAY_MS)
                    // If user has already pressed Continue and state changed, do nothing
                    val latest = uiState.value as? QuizScreenState.Content ?: return@launch
                    val questionState = latest.currentQuestionState
                    val isNotProgress =
                        questionState !is QuizScreenState.Content.QuestionState.Progress
                    val isNotEnd = questionState !is QuizScreenState.Content.QuestionState.End

                    if (isNotProgress && isNotEnd) {
                        goToNext()
                    }
                }
            }
        }
    }

    private fun goToNext() {
        val s = _ui.value as? QuizScreenState.Content ?: return
        val next = s.currentIndex + 1
        if (next >= s.totalQuestions) {
            _ui.value = s.copy(currentQuestionState = QuizScreenState.Content.QuestionState.End)
        } else {
            _ui.value = s.copy(
                currentIndex = next,
                question = items[next],
                currentQuestionState = QuizScreenState.Content.QuestionState.Progress
            )
        }
    }
}