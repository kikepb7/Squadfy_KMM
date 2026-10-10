package com.kikepb.club.presentation.join

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.text.style.TextAlign
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarBackButton
import com.kikepb.core.designsystem.triangulacion.components.InviteCodeField
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.club.presentation.generated.resources.join_create_instead
import squadfy_app.feature.club.presentation.generated.resources.join_profile_title
import squadfy_app.feature.club.presentation.generated.resources.join_title
import squadfy_app.feature.club.presentation.generated.resources.join_heading
import squadfy_app.feature.club.presentation.generated.resources.join_subtitle
import squadfy_app.feature.club.presentation.generated.resources.join_code_label
import squadfy_app.feature.club.presentation.generated.resources.join_code_placeholder
import squadfy_app.feature.club.presentation.generated.resources.join_code_hint
import squadfy_app.feature.club.presentation.generated.resources.join_shirt_label
import squadfy_app.feature.club.presentation.generated.resources.join_shirt_placeholder
import squadfy_app.feature.club.presentation.generated.resources.join_shirt_hint
import squadfy_app.feature.club.presentation.generated.resources.join_submit
import com.kikepb.club.presentation.components.PositionChips
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.join_position_title

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import com.kikepb.club.presentation.join.JoinClubAction.OnJoinClub
import com.kikepb.club.presentation.join.JoinClubEvent.Error
import com.kikepb.club.presentation.join.JoinClubEvent.Success

@Composable
fun JoinClubRoot(
    onBackClick: () -> Unit,
    onCreateClubClick: (() -> Unit)? = null,
    onSuccess: (clubId: String) -> Unit,
    viewModel: JoinClubViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is Success -> onSuccess(event.clubId)
            is Error -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asStringAsync())
            }
        }
    }

    JoinClubScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onCreateClubClick = onCreateClubClick,
        snackbarHostState = snackbarHostState
    )
}

/** Unirse (spec 018, screen 10): the code in cells, then my shirt number and position for this club. */
@Composable
fun JoinClubScreen(
    state: JoinClubState,
    onAction: (JoinClubAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onCreateClubClick: (() -> Unit)? = null
) {
    val colors = Triangulacion.colors
    val code = state.invitationCodeState.text.toString()
    Scaffold(
        containerColor = colors.surface100,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SquadfyTopBar(title = { }, navigationIcon = { SquadfyTopBarBackButton(onClick = onBackClick) }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.s5, vertical = Space.s2),
            verticalArrangement = Arrangement.spacedBy(Space.s5)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = stringResource(Res.string.join_heading), style = Triangulacion.type.displayLg, color = colors.ink)
                Text(text = stringResource(Res.string.join_subtitle), style = Triangulacion.type.body, color = colors.inkMuted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InviteCodeField(
                    value = code,
                    onValueChange = { state.invitationCodeState.setTextAndPlaceCursorAtEnd(it) },
                    isError = state.invitationCodeError != null,
                    onDone = { if (state.canSubmit) onAction(OnJoinClub) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = state.invitationCodeError?.asString() ?: stringResource(Res.string.join_code_hint),
                    style = Triangulacion.type.bodySm,
                    color = if (state.invitationCodeError != null) colors.danger else colors.inkMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s4) {
                TriCaps(text = stringResource(Res.string.join_profile_title))
                Spacer(Modifier.height(Space.s3))
                SquadfyTextField(
                    state = state.shirtNumberState,
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.join_shirt_label),
                    placeholder = stringResource(Res.string.join_shirt_placeholder),
                    singleLine = true,
                    isError = state.shirtNumberError != null,
                    supportingText = state.shirtNumberError?.asString() ?: stringResource(Res.string.join_shirt_hint),
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(Space.s4))
                TriCaps(text = stringResource(Res.string.join_position_title))
                Spacer(Modifier.height(Space.s2))
                PositionChips(
                    selected = state.position,
                    onSelected = { onAction(JoinClubAction.OnPositionSelected(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TriButton(
                    text = stringResource(Res.string.join_submit),
                    onClick = { onAction(OnJoinClub) },
                    enabled = state.canSubmit,
                    isLoading = state.isLoading,
                    large = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onCreateClubClick != null) {
                    TriTextButton(text = stringResource(Res.string.join_create_instead), onClick = onCreateClubClick, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "10 Unirse con código · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun JoinTizaPreview() = ScreenPreview { JoinClubScreen(state = JoinClubState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onCreateClubClick = {}) }

@Preview(name = "10 Unirse con código · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun JoinNochePreview() = ScreenPreview(darkTheme = true) { JoinClubScreen(state = JoinClubState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onCreateClubClick = {}) }
