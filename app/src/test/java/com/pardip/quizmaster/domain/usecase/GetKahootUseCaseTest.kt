package com.pardip.quizmaster.domain.usecase

import com.pardip.quizmaster.core.net.ErrorType
import com.pardip.quizmaster.core.net.NetworkResult
import com.pardip.quizmaster.domain.model.*
import com.pardip.quizmaster.domain.repository.KahootRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class GetKahootUseCaseTest {

    @Test
    fun `Success passes through domain list unchanged`() = runTest {
        val questions: List<Question> = mockQuestion()
        val repo = FakeRepo(NetworkResult.Success(questions))
        val useCase = GetKahootUseCase(repo)

        val result = useCase("any-id")
        assertTrue(result is NetworkResult.Success)

        val data = (result as NetworkResult.Success).data
        assertEquals(3, data.size)
        assertTrue(data[0] is MultipleChoice)
        assertTrue(data[1] is OpenEnded)
        assertTrue(data[2] is Slider)
        assertEquals(30_000, (data[0] as MultipleChoice).duration.inWholeMilliseconds)
    }

    @Test
    fun `Error is propagated unchanged`() = runTest {
        val repo = FakeRepo(NetworkResult.Error(ErrorType.Timeout))
        val useCase = GetKahootUseCase(repo)

        val result = useCase("any-id")
        assertTrue(result is NetworkResult.Error)
        assertEquals(ErrorType.Timeout, (result as NetworkResult.Error).type)
    }

    @Test(expected = CancellationException::class)
    fun `Cancellation propagates`() = runTest {
        val repo = object : KahootRepository {
            override suspend fun load(id: String): NetworkResult<List<Question>> {
                // Suspend forever; the withTimeout below will cancel this
                suspendCancellableCoroutine<Unit> { /* never resumes */ }
                error("unreachable")
            }
        }
        val useCase = GetKahootUseCase(repo)

        // JUnit will pass when a (Timeout)CancellationException is thrown
        withTimeout(50) { useCase("any-id") }
    }

    private class FakeRepo(
        private val result: NetworkResult<List<Question>>
    ) : KahootRepository {
        override suspend fun load(id: String): NetworkResult<List<Question>> = result
    }

    private fun mockQuestion(): List<Question> = listOf(
        MultipleChoice(
            text = "Which still exists?",
            imageUrl = null,
            duration = 30_000.milliseconds,
            altText = null,
            choices = listOf(
                Choice("The Great Pyramid of Giza", isCorrect = true),
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
}

