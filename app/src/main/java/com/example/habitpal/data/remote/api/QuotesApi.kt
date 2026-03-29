package com.example.habitpal.data.remote.api

import com.example.habitpal.data.remote.dto.QuoteDto
import retrofit2.http.GET

interface QuotesApi {

    @GET("random")
    suspend fun getRandomQuote(): List<QuoteDto>
}