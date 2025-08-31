package com.pardip.quizmaster.data.repository

import com.pardip.quizmaster.core.net.ErrorType
import com.pardip.quizmaster.core.net.NetworkResult
import com.pardip.quizmaster.core.util.QuizConfig
import com.pardip.quizmaster.data.createRetrofitApi
import com.pardip.quizmaster.data.remote.KahootApi
import com.pardip.quizmaster.data.mockKahootJson
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds


class KahootRepositoryImplTest {

    private lateinit var server: MockWebServer

    private lateinit var api: KahootApi

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        api = createRetrofitApi(server.url("/").toString())
    }

    @After fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `test when api call get success`() = runTest {
        server.enqueue(MockResponse().setBody(mockKahootJson()).setResponseCode(200))


        val repo = KahootRepositoryImpl(api)

        val result = repo.load(id = QuizConfig.DEFAULT_ID)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `test HTTP 500 error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val repo = KahootRepositoryImpl(api = api)
        val result = repo.load("any.json")
        assert(result is NetworkResult.Error)
        assert((result as NetworkResult.Error).type == ErrorType.Http(500))
    }

    @Test
    fun `test parsing error`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"title":"x","questions":[{"type":"quiz"}]}""") // missing required fields
        )
        val repo = KahootRepositoryImpl(api = api)
        val result = repo.load("Not important")
        assert(result is NetworkResult.Error)
        assert((result as NetworkResult.Error).type == ErrorType.Parse)
    }

    @Test
    fun `test network failure error`() = runTest {
        // Kill the server so the port refuses connection → ConnectException
        val deadPort = server.port
        server.shutdown()

        val deadApi = createRetrofitApi(baseUrl = "http://127.0.0.1:$deadPort/")
        val repo = KahootRepositoryImpl(api = deadApi)

        val result = repo.load("Not important")

        assert(result is NetworkResult.Error)
        assert((result as NetworkResult.Error).type == ErrorType.Network)
    }
    @Test(expected = CancellationException::class)
    fun `test CancellationException error`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val repo = KahootRepositoryImpl(api)

        withTimeout(50.milliseconds) {
            repo.load("Not important")
        }
    }
}
