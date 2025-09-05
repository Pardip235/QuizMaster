package com.pardip.quizmaster.data.repository


import com.pardip.quizmaster.data.remote.KahootApi
import com.pardip.quizmaster.core.net.ErrorType
import com.pardip.quizmaster.core.net.NetworkResult
import com.pardip.quizmaster.data.converter.asDomainModel
import com.pardip.quizmaster.domain.model.Question
import com.pardip.quizmaster.domain.repository.KahootRepository
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class KahootRepositoryImpl @Inject constructor(
    private val api: KahootApi
) : KahootRepository {

    override suspend fun load(id: String): NetworkResult<List<Question>> = try {
        val result = api.getKahoot(id = id)
        NetworkResult.Success(data = result.asDomainModel())
    } catch (t: Throwable) {
        t.asDomainErrorType()
    }
}


/**
 * Maps a thrown exception to a categorized [NetworkResult.Error].
 *
 * Converts HTTP, offline/timeout/network, and parse errors into [ErrorType]s
 * (preserving HTTP status via [ErrorType.Http]).
 *
 */
private fun Throwable.asDomainErrorType(): NetworkResult.Error = when (this) {
    is HttpException -> NetworkResult.Error(type = ErrorType.Http(code()))
    is UnknownHostException -> NetworkResult.Error(type = ErrorType.Offline)
    is SocketTimeoutException -> NetworkResult.Error(type = ErrorType.Timeout)
    is ConnectException -> NetworkResult.Error(type = ErrorType.Network)
    is IOException -> NetworkResult.Error(type = ErrorType.Network)
    is SerializationException -> NetworkResult.Error(type = ErrorType.Parse)
    else -> NetworkResult.Error(type = ErrorType.Unexpected)
}