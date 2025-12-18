package com.kikepb.chat.presentation.components.manage_chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.kikepb.chat.presentation.components.ChatParticipantSearchTextSection
import com.kikepb.chat.presentation.components.ChatParticipantsSelectionSection
import com.kikepb.chat.presentation.components.ManageChatButtonSection
import com.kikepb.chat.presentation.components.ManageChatHeaderRow
import com.kikepb.chat.presentation.create_chat.ManageChatAction
import com.kikepb.chat.presentation.create_chat.ManageChatAction.OnDismissDialog
import com.kikepb.chat.presentation.create_chat.ManageChatState
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.divider.SquadfyHorizontalDivider
import com.kikepb.core.presentation.util.DeviceConfiguration
import com.kikepb.core.presentation.util.clearFocusOnTap
import com.kikepb.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.cancel

@Composable
fun ManageChatScreen(
    headerText: String,
    primaryButtonText: String,
    state: ManageChatState,
    onAction: (ManageChatAction) -> Unit,
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val imeHeight = WindowInsets.ime.getBottom(density = LocalDensity.current)
    val isKeyboardVisible = imeHeight > 0
    val configuration = currentDeviceConfiguration()

    val shouldHideHeader = configuration == DeviceConfiguration.MOBILE_LANDSCAPE
            || (isKeyboardVisible && configuration != DeviceConfiguration.DESKTOP) || isTextFieldFocused

    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding()
            .background(color = MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(
            visible = !shouldHideHeader
        ) {
            Column {
                ManageChatHeaderRow(
                    title = headerText,
                    onCloseClick = {
                        onAction(OnDismissDialog)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                SquadfyHorizontalDivider()
            }
        }
        ChatParticipantSearchTextSection(
            queryState = state.queryTextState,
            onAddClick = {
                onAction(ManageChatAction.OnAddClick)
            },
            isSearchEnabled = state.canAddParticipant,
            isLoading = state.isSearching,
            modifier = Modifier
                .fillMaxWidth(),
            error = state.searchError,
            onFocusChanged = {
                isTextFieldFocused = it
            }
        )
        SquadfyHorizontalDivider()
        ChatParticipantsSelectionSection(
            existingParticipants = state.existingChatParticipants,
            selectedParticipants = state.selectedChatParticipants,
            modifier = Modifier
                .fillMaxWidth(),
            searchResult = state.currentSearchResult
        )
        SquadfyHorizontalDivider()
        ManageChatButtonSection(
            primaryButton = {
                SquadfyButton(
                    text = primaryButtonText,
                    onClick = {
                        onAction(ManageChatAction.OnPrimaryActionClick)
                    },
                    enabled = state.selectedChatParticipants.isNotEmpty(),
                    isLoading = state.isCreatingChat
                )
            },
            secondaryButton = {
                SquadfyButton(
                    text = stringResource(RString.cancel),
                    onClick = {
                        onAction(OnDismissDialog)
                    },
                    style = SquadfyButtonStyle.SECONDARY
                )
            },
            error = state.createChatError?.asString(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}