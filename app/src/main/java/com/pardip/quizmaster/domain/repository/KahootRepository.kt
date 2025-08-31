package com.pardip.quizmaster.domain.repository

import com.pardip.quizmaster.core.net.NetworkResult
import com.pardip.quizmaster.domain.model.Question

interface KahootRepository {
    suspend fun load(id: String): NetworkResult<List<Question>>
}