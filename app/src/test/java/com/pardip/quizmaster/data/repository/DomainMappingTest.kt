package com.pardip.quizmaster.data.repository

import com.pardip.quizmaster.data.converter.asDomainModel
import com.pardip.quizmaster.data.model.KahootResponse
import com.pardip.quizmaster.domain.model.MultipleChoice
import com.pardip.quizmaster.domain.model.OpenEnded
import com.pardip.quizmaster.domain.model.Slider
import com.pardip.quizmaster.data.mockKahootJson
import kotlinx.serialization.json.Json
import org.junit.Assert
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `maps multiple choice and slider questions correctly`() {

        val payload = mockKahootJson()


        val dto = json.decodeFromString<KahootResponse>(payload)
        val domain = dto.asDomainModel()

        assertEquals(3, domain.size)

        // Multiple Choice type
        val q1 = domain[0] as MultipleChoice
        assertEquals("Which still exists?", q1.text)
        assertEquals(0, q1.choices.indexOfFirst { it.isCorrect }
            .coerceAtLeast(-1)
        )
        val correctIndices = q1.choices
            .mapIndexedNotNull { i, c -> if (c.isCorrect) i else null }
            .toSet()

        Assert.assertTrue(correctIndices.contains(0))
        Assert.assertTrue(correctIndices.contains(0))
        assertEquals(30000, q1.duration.inWholeMilliseconds)

        // Open-ended type
        val q2 = domain[1] as OpenEnded
        assertEquals("The Colossus of Rhodes was based on which god?", q2.text)
        assertEquals(60000, q2.duration.inWholeMilliseconds)
        // Accepted answers preserve order as provided by JSON
        assertEquals(listOf("Helios", "helios"), q2.acceptedAnswers)
        Assert.assertTrue(q2.acceptedAnswers.any { it.equals("helios", ignoreCase = true) })

        // Slider question type
        val q3 = domain[2] as Slider
        assertEquals(0.0, q3.start)
        assertEquals(7.0, q3.end)
        assertEquals(1.0, q3.step)
        assertEquals(1.0, q3.correct)
        assertEquals(0.0, q3.tolerance)
        assertEquals(20000, q3.duration.inWholeMilliseconds)
    }
}