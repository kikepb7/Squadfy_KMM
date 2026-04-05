package com.kikepb.globalPosition.presentation.model

data class NewsUiModel(
    val id: String,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val publishedAt: String,
    val category: String,
    val source: String
)
