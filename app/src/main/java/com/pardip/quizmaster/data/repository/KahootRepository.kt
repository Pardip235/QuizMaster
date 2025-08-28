package com.pardip.quizmaster.data.repository


import com.pardip.quizmaster.data.remote.api.KahootApi
import com.pardip.quizmaster.data.remote.dto.KahootResponse
import com.pardip.quizmaster.data.remote.network.ApiProvider
import com.pardip.quizmaster.data.util.ErrorType
import com.pardip.quizmaster.data.util.NetworkResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class KahootRepository(
    private val api: KahootApi = ApiProvider.api
) {
    /**
     * API method returns the body directly (KahootResponse).
     * - Non-2xx -> HttpException
     * - Missing required DTO field -> SerializationException
     */
    suspend fun load(id: String): NetworkResult<KahootResponse> = try {
        val dto = api.getKahoot(id)

        // Optional integrity check: make sure backend returned what we asked for.
        if (!dto.uuid.equals(id, ignoreCase = true)) {
            NetworkResult.Error(ErrorType.Unexpected)
        } else {
            NetworkResult.Success(dto)
        }
    } catch (ce: CancellationException) {
        throw ce
    } catch (t: Throwable) {
        t.toError()
    }
}

/**
 * Maps a thrown exception to a categorized [NetworkResult.Error].
 *
 * Converts HTTP, offline/timeout/network, and parse errors into [ErrorType]s
 * (preserving HTTP status via [ErrorType.Http]). Do not use for
 * CancellationException—let it propagate.
 */
private fun Throwable.toError(): NetworkResult.Error = when (this) {
    is HttpException -> NetworkResult.Error(type = ErrorType.Http(code()))
    is UnknownHostException -> NetworkResult.Error(type = ErrorType.Offline)
    is SocketTimeoutException -> NetworkResult.Error(type = ErrorType.Timeout)
    is ConnectException -> NetworkResult.Error(type = ErrorType.Network)
    is IOException -> NetworkResult.Error(type = ErrorType.Network)
    is SerializationException -> NetworkResult.Error(type = ErrorType.Parse)
    else -> NetworkResult.Error(type = ErrorType.Unexpected)
}