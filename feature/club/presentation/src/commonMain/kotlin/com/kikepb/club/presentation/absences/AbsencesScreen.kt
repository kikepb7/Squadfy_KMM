package com.kikepb.club.presentation.absences

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarBackButton
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarIconButton
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.format.formatDayMonth
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.club.presentation.generated.resources.absences_club_caps
import squadfy_app.feature.club.presentation.generated.resources.absences_mine_caps
import squadfy_app.feature.club.presentation.generated.resources.absences_notice_body
import squadfy_app.feature.club.presentation.generated.resources.absences_notice_title
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.presentation.components.DatePickerDialog
import com.kikepb.club.presentation.components.HintText
import com.kikepb.club.presentation.components.SectionCard
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.absences_add
import squadfy_app.feature.club.presentation.generated.resources.absences_delete
import squadfy_app.feature.club.presentation.generated.resources.absences_delete_description
import squadfy_app.feature.club.presentation.generated.resources.absences_delete_title
import squadfy_app.feature.club.presentation.generated.resources.absences_empty
import squadfy_app.feature.club.presentation.generated.resources.absences_from
import squadfy_app.feature.club.presentation.generated.resources.absences_period
import squadfy_app.feature.club.presentation.generated.resources.absences_reason
import squadfy_app.feature.club.presentation.generated.resources.absences_title
import squadfy_app.feature.club.presentation.generated.resources.absences_to
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_save
import squadfy_app.feature.club.presentation.generated.resources.extra_match_pick
import squadfy_app.feature.club.presentation.generated.resources.former_member
import kotlin.time.Clock

@Composable
fun AbsencesRoot(onBackClick: () -> Unit, viewModel: AbsencesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AbsencesEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
        }
    }
    AbsencesScreen(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsencesScreen(state: AbsencesState, onAction: (AbsencesAction) -> Unit, onBackClick: () -> Unit, snackbarHostState: SnackbarHostState) {
    Scaffold(
        containerColor = Triangulacion.colors.surface100,
        topBar = {
            SquadfyTopBar(
                title = { Text(text = stringResource(Res.string.absences_title), style = Triangulacion.type.displayMd, color = Triangulacion.colors.ink) },
                navigationIcon = { SquadfyTopBarBackButton(onClick = onBackClick) },
                actions = { SquadfyTopBarIconButton(onClick = { onAction(AbsencesAction.OnAddClick) }, icon = TriIcon.Plus, contentDescription = stringResource(Res.string.absences_add)) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onAction(AbsencesAction.OnRefresh) }, modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "notice") {
                    TriNotice(title = stringResource(Res.string.absences_notice_title), body = stringResource(Res.string.absences_notice_body), tone = TriNoticeTone.Warn)
                }
                if (state.isLoading && state.absences.isEmpty()) item(key = "loading") { SquadfyLoadingIndicator() }
                if (!state.isLoading && state.absences.isEmpty()) item(key = "empty") { HintText(text = stringResource(Res.string.absences_empty)) }
                val (mine, others) = state.absences.partition { state.canDelete(it) }
                if (mine.isNotEmpty()) {
                    item(key = "mine-title") { TriCaps(text = stringResource(Res.string.absences_mine_caps), modifier = Modifier.padding(top = 8.dp)) }
                    items(items = mine, key = { it.id }) { absence -> AbsenceRow(absence = absence, state = state, onAction = onAction) }
                }
                if (others.isNotEmpty()) {
                    item(key = "club-title") { TriCaps(text = stringResource(Res.string.absences_club_caps), modifier = Modifier.padding(top = 8.dp)) }
                    items(items = others, key = { it.id }) { absence -> AbsenceRow(absence = absence, state = state, onAction = onAction) }
                }
            }
        }
    }
    AbsenceDialogs(state = state, onAction = onAction)
}

@Composable
private fun AbsenceRow(absence: MemberAbsenceModel, state: AbsencesState, onAction: (AbsencesAction) -> Unit) {
    val colors = Triangulacion.colors
    val name = state.members[absence.clubMemberId]?.username ?: stringResource(Res.string.former_member)
    val mine = state.canDelete(absence)
    TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (mine) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp).background(colors.warningSoft, CircleShape)) {
                    TriIconView(icon = TriIcon.Moon, contentDescription = null, size = 22.dp, tint = colors.warning)
                }
            } else {
                TriAvatar(name = name, imageUrl = state.members[absence.clubMemberId]?.pictureUrl, size = 44.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (mine) "${formatDayMonth(absence.fromDate)} – ${formatDayMonth(absence.toDate)}" else name,
                    style = Triangulacion.type.title,
                    color = colors.ink
                )
                Text(
                    text = listOfNotNull(
                        if (mine) null else "${formatDayMonth(absence.fromDate)} – ${formatDayMonth(absence.toDate)}",
                        absence.reason
                    ).joinToString(" · "),
                    style = Triangulacion.type.bodySm,
                    color = colors.inkMuted
                )
            }
            if (mine) {
                TriRoundIconButton(icon = TriIcon.Trash, contentDescription = stringResource(Res.string.absences_delete), onClick = { onAction(AbsencesAction.OnDeleteClick(absence)) }, filled = true)
            } else {
                TriIconView(icon = TriIcon.Moon, contentDescription = null, size = 20.dp, tint = colors.inkMuted)
            }
        }
    }
}

@Composable
private fun AbsenceDialogs(state: AbsencesState, onAction: (AbsencesAction) -> Unit) {
    state.confirmDelete?.let {
        SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.absences_delete_title),
            description = stringResource(Res.string.absences_delete_description),
            confirmButtonText = stringResource(Res.string.absences_delete),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(AbsencesAction.OnConfirmDelete) },
            onCancelClick = { onAction(AbsencesAction.OnDismissDelete) },
            onDismiss = { onAction(AbsencesAction.OnDismissDelete) }
        )
    }
    val form = state.form ?: return
    state.picker?.let { target ->
        val today = Clock.System.now().toLocalDateTime(state.zone).date
        DatePickerDialog(
            isSelectable = { date -> if (target == AbsenceDateTarget.TO) date >= maxOf(today, form.fromDate) else true },
            onConfirm = { onAction(AbsencesAction.OnDatePicked(target, it)) },
            onDismiss = { onAction(AbsencesAction.OnDismissPicker) }
        )
        return
    }
    AlertDialog(
        onDismissRequest = { onAction(AbsencesAction.OnDismissForm) },
        title = { Text(text = stringResource(Res.string.absences_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PickRow(text = stringResource(Res.string.absences_from, form.fromDate.formatted())) { onAction(AbsencesAction.OnOpenPicker(AbsenceDateTarget.FROM)) }
                PickRow(text = stringResource(Res.string.absences_to, form.toDate.formatted())) { onAction(AbsencesAction.OnOpenPicker(AbsenceDateTarget.TO)) }
                SquadfyTextField(state = form.reason, title = stringResource(Res.string.absences_reason))
                form.error?.let { Text(text = it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { TextButton(onClick = { onAction(AbsencesAction.OnSave) }, enabled = !state.isWorking) { Text(text = stringResource(Res.string.common_save)) } },
        dismissButton = { TextButton(onClick = { onAction(AbsencesAction.OnDismissForm) }) { Text(text = stringResource(Res.string.common_cancel)) } }
    )
}

@Composable
private fun PickRow(text: String, onPick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onPick) { Text(text = stringResource(Res.string.extra_match_pick)) }
    }
}

private fun LocalDate.formatted(): String = "${day.toString().padStart(2, '0')}/${(month.ordinal + 1).toString().padStart(2, '0')}/$year"

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "12 Ausencias · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun AbsencesTizaPreview() = ScreenPreview { AbsencesScreen(state = AbsencesState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "12 Ausencias · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun AbsencesNochePreview() = ScreenPreview(darkTheme = true) { AbsencesScreen(state = AbsencesState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }
