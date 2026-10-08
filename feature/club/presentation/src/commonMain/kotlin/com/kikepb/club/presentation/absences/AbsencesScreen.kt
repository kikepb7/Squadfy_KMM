package com.kikepb.club.presentation.absences

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
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        topBar = { SquadfyTopBar(title = stringResource(Res.string.absences_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onAction(AbsencesAction.OnRefresh) }, modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "add") {
                    SquadfyButton(text = stringResource(Res.string.absences_add), onClick = { onAction(AbsencesAction.OnAddClick) }, modifier = Modifier.fillMaxWidth())
                }
                if (state.isLoading && state.absences.isEmpty()) item(key = "loading") { SquadfyLoadingIndicator() }
                if (!state.isLoading && state.absences.isEmpty()) item(key = "empty") { HintText(text = stringResource(Res.string.absences_empty)) }
                items(items = state.absences, key = { it.id }) { absence -> AbsenceRow(absence = absence, state = state, onAction = onAction) }
            }
        }
    }
    AbsenceDialogs(state = state, onAction = onAction)
}

@Composable
private fun AbsenceRow(absence: MemberAbsenceModel, state: AbsencesState, onAction: (AbsencesAction) -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.members[absence.clubMemberId]?.username ?: stringResource(Res.string.former_member),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                HintText(text = stringResource(Res.string.absences_period, absence.fromDate.formatted(), absence.toDate.formatted()))
                absence.reason?.let { HintText(text = it) }
            }
            if (state.canDelete(absence)) {
                IconButton(onClick = { onAction(AbsencesAction.OnDeleteClick(absence)) }) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(Res.string.absences_delete))
                }
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
