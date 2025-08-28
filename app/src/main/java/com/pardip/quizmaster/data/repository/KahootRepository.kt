package com.pardip.quizmaster.data.repository


import com.pardip.quizmaster.data.remote.network.ApiProvider
import com.pardip.quizmaster.data.util.NetworkResult
import com.pardip.quizmaster.data.remote.dto.KahootResponse
import com.pardip.quizmaster.data.remote.api.KahootApi
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class KahootRepository(
    private val api: KahootApi = ApiProvider.api
) {
    suspend fun load(id: String): NetworkResult<KahootResponse> = try {
        NetworkResult.Success(api.getKahoot(id))
    } catch (ce: CancellationException) {
        throw ce
    } catch (t: Throwable) {
        t.toError()
    }
}

private fun Throwable.toError(): NetworkResult.Error = when (this) {
    is HttpException -> NetworkResult.Error(this.code(), this.message())
    is IOException -> NetworkResult.Error(null, "Network error")
    is SerializationException -> NetworkResult.Error(null, "Parse error")
    else -> NetworkResult.Error(null, "Unexpected: ${message ?: this::class.simpleName}")
}