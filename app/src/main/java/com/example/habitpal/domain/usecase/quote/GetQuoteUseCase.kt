package com.example.habitpal.domain.usecase.quote

import com.example.habitpal.domain.model.Quote
import com.example.habitpal.domain.repository.QuoteRepository
import com.example.habitpal.util.Resource
import javax.inject.Inject

class GetQuoteUseCase @Inject constructor(
    private val quoteRepository: QuoteRepository
) {
    suspend operator fun invoke(): Resource<Quote> = quoteRepository.getRandomQuote()
}

