package com.kikepb.globalPosition.presentation.mapper

import com.kikepb.globalPosition.domain.model.NewsModel
import com.kikepb.globalPosition.presentation.model.NewsUiModel

fun NewsModel.toUiModel() = NewsUiModel(
    id = id,
    title = title,
    summary = summary,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    category = category,
    source = source
)
