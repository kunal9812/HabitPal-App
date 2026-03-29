package com.example.habitpal.data.mapper

import com.example.habitpal.data.remote.dto.QuoteDto
import com.example.habitpal.domain.model.Quote

fun QuoteDto.toDomain(): Quote = Quote(
    id = "",
    content = content,
    author = author
)

