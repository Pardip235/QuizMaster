package com.pardip.quizmaster.core.net

/**
 * Simple result wrapper for repository calls.
 *
 * Repos return either [Success] with data or [Error] with an [ErrorType].
 * UI maps ErrorType to strings; no exceptions leak to UI.
 */
sealed interface NetworkResult<out T> {
    /** Parsed response body. */
    data class Success<T>(val data: T) : NetworkResult<T>

    /** Categorized failure (HTTP, timeout, offline, etc.). */
    data class Error(val type: ErrorType) : NetworkResult<Nothing>
}