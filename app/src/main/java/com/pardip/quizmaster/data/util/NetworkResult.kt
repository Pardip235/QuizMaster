package com.pardip.quizmaster.data.util

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T): NetworkResult<T>
    data class Error(val code: Int?, val message: String): NetworkResult<Nothing>
}