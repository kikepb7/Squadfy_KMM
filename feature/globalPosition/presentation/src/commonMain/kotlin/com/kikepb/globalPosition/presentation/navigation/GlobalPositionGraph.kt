package com.kikepb.globalPosition.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kikepb.globalPosition.presentation.GlobalPositionRoot
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes.GlobalPosition
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes.GlobalPositionGraph

fun NavGraphBuilder.globalPositionGraph() {
    navigation<GlobalPositionGraph>(
        startDestination = GlobalPosition
    ) {
        composable<GlobalPosition> {
            GlobalPositionRoot()
        }
    }
}
