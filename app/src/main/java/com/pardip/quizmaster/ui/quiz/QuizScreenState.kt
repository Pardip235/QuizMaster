package com.pardip.quizmaster.ui.quiz

import com.pardip.quizmaster.core.net.ErrorType
import com.pardip.quizmaster.domain.model.Question

sealed interface QuizScreenState {

    data object Loading : QuizScreenState

    data class Error(val error: ErrorType) : QuizScreenState

    data class Content(
        val currentIndex: Int,
        val totalQuestions: Int,
        val question: Question?,
        val currentQuestionState: QuestionState
    ) : QuizScreenState {

        sealed interface QuestionState {
            data object Progress : QuestionState

            data class TimesUp(val correctIndex: Int) : QuestionState

            sealed interface Answered : QuestionState {
                data class Correct(val selectedIndex: Int) : Answered

                data class Wrong(val selectedIndex: Int, val correctIndex: Int) : Answered

                data class InputText(val text: String) : Answered

                data class InputNumber(val value: Int) : Answered

                fun selectedIndex(): Int? = when (this) {
                    is Correct -> selectedIndex
                    is Wrong -> selectedIndex
                    else -> null
                }
            }

            data object End : QuestionState

            fun showSolution() = this !is Progress
        }
    }
}
