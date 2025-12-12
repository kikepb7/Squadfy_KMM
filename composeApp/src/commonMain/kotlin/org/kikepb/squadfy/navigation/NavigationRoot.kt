package org.kikepb.squadfy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.kikepb.auth.presentation.navigation.AuthGraphNavigation
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Graph
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Login
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val navigationState = rememberNavigationState(
        startRoute = Graph,
        topLevelRoutes = setOf(Graph)
    )
    val navigator = remember { Navigator(state = navigationState) }

    val appEntryProvider = entryProvider {
        entry<AuthGraphRoutes.Graph> { key ->
            AuthGraphNavigation(
                onLoginSuccess = {
                    navigator.navigate(route = Login)
                }
            )
        }
    }


    val rootBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Graph::class, Graph.serializer())
                }
            }
        },
        Graph
    )

    NavDisplay(
        modifier = modifier,
        backStack = rootBackStack,
        onBack = { navigator.goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Graph> {
                AuthGraphNavigation(
                    onLoginSuccess = {
                        rootBackStack.remove(element = Graph)
                        rootBackStack.add(Login)
                    }
                )
            }
        }
    )
}