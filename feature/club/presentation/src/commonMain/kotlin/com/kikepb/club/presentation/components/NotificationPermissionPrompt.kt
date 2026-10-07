package com.kikepb.club.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.kikepb.core.domain.notification.NotificationPromptStore
import com.kikepb.core.presentation.permissions.Permission
import com.kikepb.core.presentation.permissions.rememberPermissionController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.notifications_permission_allow
import squadfy_app.feature.club.presentation.generated.resources.notifications_permission_later
import squadfy_app.feature.club.presentation.generated.resources.notifications_permission_text
import squadfy_app.feature.club.presentation.generated.resources.notifications_permission_title

/** AC-009-01: the first time a club is opened, explain the match notifications before the system prompt. */
@Composable
fun NotificationPermissionPrompt(store: NotificationPromptStore = koinInject()) {
    val controller = rememberPermissionController()
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = !store.wasAsked() && !controller.isGranted(Permission.NOTIFICATIONS)
    }

    if (!visible) return
    AlertDialog(
        onDismissRequest = {
            visible = false
            scope.launch { store.markAsked() }
        },
        title = { Text(text = stringResource(Res.string.notifications_permission_title)) },
        text = { Text(text = stringResource(Res.string.notifications_permission_text)) },
        confirmButton = {
            TextButton(onClick = {
                visible = false
                scope.launch {
                    store.markAsked()
                    controller.requestPermission(Permission.NOTIFICATIONS)
                }
            }) { Text(text = stringResource(Res.string.notifications_permission_allow)) }
        },
        dismissButton = {
            TextButton(onClick = {
                visible = false
                scope.launch { store.markAsked() }
            }) { Text(text = stringResource(Res.string.notifications_permission_later)) }
        }
    )
}
