package com.example.habitpal.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuoteDto(
    @Json(name = "q") val content: String,
    @Json(name = "a") val author: String,
    @Json(name = "h") val html: String
)