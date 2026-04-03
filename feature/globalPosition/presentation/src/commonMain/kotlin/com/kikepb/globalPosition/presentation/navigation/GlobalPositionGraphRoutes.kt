package com.kikepb.globalPosition.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface GlobalPositionGraphRoutes {

    @Serializable
    data object GlobalPositionGraph : GlobalPositionGraphRoutes

    @Serializable
    data object GlobalPosition : GlobalPositionGraphRoutes
}
