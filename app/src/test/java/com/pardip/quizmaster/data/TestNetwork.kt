package com.pardip.quizmaster.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pardip.quizmaster.data.remote.KahootApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

private val testJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

fun createRetrofitApi(baseUrl: String): KahootApi {
    val contentType = "application/json".toMediaType()
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(testJson.asConverterFactory(contentType))
        .build()
        .create(KahootApi::class.java)
}

/** Minimal but valid payload. */
fun mockKahootJson(): String = """
{
  "title":"Seven Wonders",
  "cover":"https://example/cover.jpg",
  "questions":[
    {
      "type":"quiz",
      "question":"Which still exists?",
      "time":30000,
      "choices":[
        {"answer":"The Great Pyramid of Giza","correct":true},
        {"answer":"The Colossus of Rhodes","correct":false}
      ],
      "media":[]
    },
    {
      "type":"open_ended",
      "question":"The Colossus of Rhodes was based on which god?",
      "time":60000,
      "choices":[
        {"answer":"Helios","correct":true},
        {"answer":"helios","correct":true}
      ],
      "media":[]
    },
    {
      "type":"slider",
      "question":"How many still exist?",
      "time":20000,
      "choiceRange":{"start":0,"end":7,"step":1,"correct":1,"tolerance":0},
      "media":[]
    }
  ]
}
""".trimIndent()
