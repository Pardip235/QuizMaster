package com.pardip.quizmaster.data.remote.api

import com.pardip.quizmaster.data.remote.dto.KahootResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface KahootApi {
    @GET("rest/kahoots/{id}")
    suspend fun getKahoot(@Path("id") id: String): KahootResponse
}