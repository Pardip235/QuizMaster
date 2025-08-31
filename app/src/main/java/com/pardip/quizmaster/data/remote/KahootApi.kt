package com.pardip.quizmaster.data.remote

import com.pardip.quizmaster.data.model.KahootResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Kahoot REST API via Retrofit.
 *
 * Base URL must end with "/", e.g. "https://create.kahoot.it/".
 */
interface KahootApi {
    /**
     * Fetch a kahoot by UUID.
     *
     * GET /rest/kahoots/{id}
     *
     * @param id Kahoot UUID (e.g., "fb4054fc-6a71-463e-88cd-243876715bc1").
     * @return parsed [KahootResponse]
     */
    @GET("rest/kahoots/{id}")
    suspend fun getKahoot(@Path("id") id: String): KahootResponse
}