package com.kikepb.globalPosition.domain.model

data class NewsModel(
    val id: String,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val publishedAt: String,
    val category: String,
    val source: String
)
