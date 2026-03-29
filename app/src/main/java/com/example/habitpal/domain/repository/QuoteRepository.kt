package com.example.habitpal.domain.repository

import com.example.habitpal.domain.model.Quote
import com.example.habitpal.util.Resource

interface QuoteRepository {
    suspend fun getRandomQuote(): Resource<Quote>
}

