package com.pardip.quizmaster.core.net

sealed class ErrorType {
    data class Http(val code: Int) : ErrorType()
    data object Offline : ErrorType() // UnknownHostException
    data object Timeout : ErrorType() // SocketTimeoutException
    data object Network : ErrorType() // other IO/connect errors
    data object Parse : ErrorType()    // SerializationException
    data object Unexpected : ErrorType()
}