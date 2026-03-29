package com.example.habitpal.data.repository

import com.example.habitpal.data.mapper.toDomain
import com.example.habitpal.data.remote.api.QuotesApi
import com.example.habitpal.domain.model.Quote
import com.example.habitpal.domain.repository.QuoteRepository
import com.example.habitpal.util.Resource
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor(
    private val quotesApi: QuotesApi
) : QuoteRepository {

    override suspend fun getRandomQuote(): Resource<Quote> {
        return try {
            val dtoList = quotesApi.getRandomQuote()
            val quote = dtoList.firstOrNull()
                ?: return Resource.Error("No quote returned")
            Resource.Success(quote.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unable to fetch quote", e)
        }
    }
}

