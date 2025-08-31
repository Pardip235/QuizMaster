@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.pardip.quizmaster.ui.quiz

import com.pardip.quizmaster.core.net.ErrorType
import com.pardip.quizmaster.core.net.NetworkResult
import com.pardip.quizmaster.domain.model.*
import com.pardip.quizmaster.domain.usecase.GetKahootUseCase
import com.pardip.quizmaster.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class QuizViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `load data with ui show Content_Progress on first question`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        val s = vm.uiState.value
        assertTrue(s is QuizScreenState.Content)
        s as QuizScreenState.Content
        assertEquals(0, s.currentIndex)
        assertTrue(s.currentQuestionState is QuizScreenState.Content.QuestionState.Progress)
        assertTrue(s.question is MultipleChoice)
    }

    @Test
    fun `failed to load data which Error state`() = runTest {
        val vm = vmWith(NetworkResult.Error(ErrorType.Timeout))
        advanceUntilIdle()

        val s = vm.uiState.value
        assertTrue(s is QuizScreenState.Error)
        assertEquals(ErrorType.Timeout, (s as QuizScreenState.Error).error)
    }

    @Test
    fun `data loaded with empty list which show Error Unexpected`() = runTest {
        val vm = vmWith(NetworkResult.Success(emptyList()))
        advanceUntilIdle()

        val s = vm.uiState.value
        assertTrue(s is QuizScreenState.Error)
        assertEquals(ErrorType.Unexpected, (s as QuizScreenState.Error).error)
    }


    @Test
    fun `Multiple choice correct with Answered_Correct`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        vm.selectAnswer(0) // correct in sample
        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Answered.Correct)
    }

    @Test
    fun `Multiple choice wrong with Answered_Wrong with correctIndex set`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        vm.selectAnswer(1) // wrong
        val c = vm.uiState.value as QuizScreenState.Content
        val st = c.currentQuestionState as QuizScreenState.Content.QuestionState.Answered.Wrong
        assertEquals(1, st.selectedIndex)
        assertEquals(0, st.correctIndex) // index 0 is correct per sample
    }

    @Test
    fun `Open ended question correct with Answered_Correct`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // Go to OE
        vm.selectAnswer(0); vm.continueNext()
        advanceUntilIdle()

        vm.submitOpenEnded("Helios")
        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Answered.Correct)
    }

    @Test
    fun `Open ended question wrong with  Answered_Wrong`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // Go to OE
        vm.selectAnswer(0); vm.continueNext()
        advanceUntilIdle()

        vm.submitOpenEnded("Poseidon")
        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Answered.Wrong)
    }


    @Test
    fun `Slider correct show Answered_Correct`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // MC -> OE -> Slider
        vm.selectAnswer(0); vm.continueNext()
        vm.submitOpenEnded("Helios"); vm.continueNext()
        advanceUntilIdle()

        vm.submitSlider(1.0) // equals correct
        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Answered.Correct)
    }

    @Test
    fun `Slider wrong show Answered_Wrong with indices`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // MC -> OE -> Slider
        vm.selectAnswer(0); vm.continueNext()
        vm.submitOpenEnded("Helios"); vm.continueNext()
        advanceUntilIdle()

        vm.submitSlider(3.0) // wrong (correct=1)
        val c = vm.uiState.value as QuizScreenState.Content
        val st = c.currentQuestionState as QuizScreenState.Content.QuestionState.Answered.Wrong
        assertEquals(3, st.selectedIndex)   // VM uses roundToInt()
        assertEquals(1, st.correctIndex)
    }

    @Test
    fun `time up on MC show TimesUp(correctIndex)`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        vm.onTimeUp()
        val c = vm.uiState.value as QuizScreenState.Content
        val ts = c.currentQuestionState as QuizScreenState.Content.QuestionState.TimesUp
        assertEquals(0, ts.correctIndex)
    }

    @Test
    fun `time up on OE show TimesUp(-1)`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // Navigate to OE while still in Progress
        vm.continueNext()
        advanceUntilIdle()

        vm.onTimeUp()
        val c = vm.uiState.value as QuizScreenState.Content
        val ts = c.currentQuestionState as QuizScreenState.Content.QuestionState.TimesUp
        assertEquals(-1, ts.correctIndex)
    }

    @Test
    fun `time up does NOT override when already answered`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        vm.selectAnswer(0) // set to Answered.Correct
        vm.onTimeUp()      // should be ignored
        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Answered.Correct)
    }

    @Test
    fun `continueNext moves to next question`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        vm.selectAnswer(0)
        vm.continueNext()
        val c = vm.uiState.value as QuizScreenState.Content
        assertEquals(1, c.currentIndex)
        assertTrue(c.question is OpenEnded)
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Progress)
    }

    @Test
    fun `auto-advance after answer switches to next question`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        vm.selectAnswer(0)
        // VM schedules 2_000ms delay. Advance virtual time:
        advanceTimeBy(2_000)
        advanceUntilIdle()

        val c = vm.uiState.value as QuizScreenState.Content
        assertEquals(1, c.currentIndex)
        assertTrue(c.question is OpenEnded)
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Progress)
    }

    @Test
    fun `after last question show End state`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // MC -> OE -> Slider, then continue once more
        vm.selectAnswer(0); vm.continueNext()
        vm.submitOpenEnded("Helios"); vm.continueNext()
        vm.submitSlider(1.0); vm.continueNext()

        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.End)
    }

    @Test
    fun `selectAnswer ignored when not Progress`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        vm.selectAnswer(0) // now Answered.Correct
        val before = vm.uiState.value as QuizScreenState.Content
        vm.selectAnswer(1) // should be ignored
        val after = vm.uiState.value as QuizScreenState.Content
        assertEquals(before.currentQuestionState, after.currentQuestionState)
    }

    @Test
    fun `submitOpenEnded ignored on non-OE`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // still on MC
        vm.submitOpenEnded("Helios") // should be ignored
        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.question is MultipleChoice)
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Progress)
    }

    @Test
    fun `submitSlider ignored on non-Slider`() = runTest {
        val vm = vmWith(NetworkResult.Success(sampleQuestions()))
        advanceUntilIdle()

        // still on MC
        vm.submitSlider(1.0) // should be ignored
        val c = vm.uiState.value as QuizScreenState.Content
        assertTrue(c.question is MultipleChoice)
        assertTrue(c.currentQuestionState is QuizScreenState.Content.QuestionState.Progress)
    }

    private fun sampleQuestions(): List<Question> = listOf(
        MultipleChoice(
            text = "Which still exists?",
            imageUrl = null,
            duration = 30_000.milliseconds,
            altText = null,
            choices = listOf(
                Choice("The Great Pyramid of Giza", isCorrect = true), // index 0 correct
                Choice("The Colossus of Rhodes", isCorrect = false)
            )
        ),
        OpenEnded(
            text = "The Colossus of Rhodes was based on which god?",
            imageUrl = null,
            duration = 60_000.milliseconds,
            altText = null,
            acceptedAnswers = listOf("Helios", "helios")
        ),
        Slider(
            text = "How many still exist?",
            imageUrl = null,
            duration = 20_000.milliseconds,
            altText = null,
            start = 0.0, end = 7.0, step = 1.0, correct = 1.0, tolerance = 0.0
        )
    )

    private fun vmWith(result: NetworkResult<List<Question>>): QuizViewModel {
        val uc: GetKahootUseCase = mockk()
        coEvery { uc.invoke(any()) } returns result
        return QuizViewModel(getKahoot = uc)
    }
}
