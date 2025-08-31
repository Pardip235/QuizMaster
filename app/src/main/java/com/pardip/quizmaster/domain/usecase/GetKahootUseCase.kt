package com.pardip.quizmaster.domain.usecase

import com.pardip.quizmaster.core.net.NetworkResult
import com.pardip.quizmaster.domain.model.Question
import com.pardip.quizmaster.domain.repository.KahootRepository
import javax.inject.Inject


class GetKahootUseCase @Inject constructor(
    private val repo: KahootRepository
) {
    suspend operator fun invoke(id: String): NetworkResult<List<Question>> = repo.load(id = id)
}
