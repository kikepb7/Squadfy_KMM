package com.kikepb.club.presentation.memberdetail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.policy.MemberPermissions
import com.kikepb.club.domain.usecase.BanMemberUseCase
import com.kikepb.club.domain.usecase.ChangeMemberRoleUseCase
import com.kikepb.club.domain.usecase.GetClubMemberByIdUseCase
import com.kikepb.club.domain.usecase.JoinClubUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.RemoveMemberUseCase
import com.kikepb.club.domain.usecase.TransferOwnershipUseCase
import com.kikepb.club.domain.usecase.UpdateMyMembershipUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_error_shirt_number
import squadfy_app.feature.club.presentation.generated.resources.member_role_changed
import squadfy_app.feature.club.presentation.generated.resources.member_updated

/** Member profile with the actions the current member may perform (spec 003, AC-003-06…09). */
class MemberDetailViewModel(
    getClubMemberByIdUseCase: GetClubMemberByIdUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val changeMemberRoleUseCase: ChangeMemberRoleUseCase,
    private val removeMemberUseCase: RemoveMemberUseCase,
    private val banMemberUseCase: BanMemberUseCase,
    private val transferOwnershipUseCase: TransferOwnershipUseCase,
    private val updateMyMembershipUseCase: UpdateMyMembershipUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")
    private val memberId = savedStateHandle.get<String>("memberId")
        ?: throw IllegalStateException("memberId is required")

    private val eventChannel = Channel<MemberDetailEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MemberDetailState())

    val state = combine(
        _state,
        getClubMemberByIdUseCase(memberId = memberId),
        observeMyMembershipUseCase(clubId = clubId)
    ) { current, member, me ->
        current.copy(member = member, me = me, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = MemberDetailState()
    )

    fun onAction(action: MemberDetailAction) {
        when (action) {
            is MemberDetailAction.OnShowDialog -> openDialog(action.dialog)
            MemberDetailAction.OnDismissDialog -> _state.update { it.copy(dialog = null, editError = null) }
            is MemberDetailAction.OnChangeRole -> changeRole(action.role)
            MemberDetailAction.OnConfirmRemove -> runOnMember(removeMemberUseCase::invoke, leavesProfile = true)
            MemberDetailAction.OnConfirmBan -> runOnMember(banMemberUseCase::invoke, leavesProfile = true)
            MemberDetailAction.OnConfirmTransfer -> transferOwnership()
            is MemberDetailAction.OnEditPositionSelected -> _state.update { it.copy(editPosition = action.position) }
            MemberDetailAction.OnSaveMyMembership -> saveMyMembership()
        }
    }

    private fun openDialog(dialog: MemberDialog) {
        val member = state.value.member
        _state.update {
            if (dialog == MemberDialog.EDIT_MINE && member != null) {
                it.copy(
                    dialog = dialog,
                    editShirtNumber = TextFieldState(initialText = member.shirtNumber?.toString().orEmpty()),
                    editPosition = member.position,
                    editError = null
                )
            } else {
                it.copy(dialog = dialog)
            }
        }
    }

    private fun changeRole(role: ClubMemberRole) = launchWorking {
        changeMemberRoleUseCase(clubId = clubId, memberId = memberId, role = role)
            .onSuccess { eventChannel.send(MemberDetailEvent.ShowMessage(UiText.Resource(Res.string.member_role_changed))) }
            .onFailure { error -> eventChannel.send(MemberDetailEvent.ShowMessage(error.toUiText())) }
    }

    private fun transferOwnership() = launchWorking {
        transferOwnershipUseCase(clubId = clubId, memberId = memberId)
            .onFailure { error -> eventChannel.send(MemberDetailEvent.ShowMessage(error.toUiText())) }
    }

    private fun runOnMember(
        operation: suspend (clubId: String, memberId: String) -> com.kikepb.core.domain.util.EmptyResult<com.kikepb.club.domain.error.ClubError>,
        leavesProfile: Boolean
    ) = launchWorking {
        operation(clubId, memberId)
            .onSuccess { if (leavesProfile) eventChannel.send(MemberDetailEvent.MemberLeftClub) }
            .onFailure { error -> eventChannel.send(MemberDetailEvent.ShowMessage(error.toUiText())) }
    }

    private fun saveMyMembership() {
        val rawShirt = _state.value.editShirtNumber.text.toString().trim()
        val shirtNumber = if (rawShirt.isBlank()) null else rawShirt.toIntOrNull()?.takeIf { it in JoinClubUseCase.VALID_SHIRT_NUMBERS }
        if (rawShirt.isNotBlank() && shirtNumber == null) {
            _state.update { it.copy(editError = UiText.Resource(Res.string.club_error_shirt_number)) }
            return
        }
        launchWorking {
            updateMyMembershipUseCase(clubId = clubId, shirtNumber = shirtNumber, position = _state.value.editPosition)
                .onSuccess { eventChannel.send(MemberDetailEvent.ShowMessage(UiText.Resource(Res.string.member_updated))) }
                .onFailure { error -> eventChannel.send(MemberDetailEvent.ShowMessage(error.toUiText())) }
        }
    }

    private fun launchWorking(block: suspend () -> Unit) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, dialog = null) }
            try {
                block()
            } finally {
                _state.update { it.copy(isWorking = false) }
            }
        }
    }
}

enum class MemberDialog { CHANGE_ROLE, REMOVE, BAN, TRANSFER, EDIT_MINE }

data class MemberDetailState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val member: ClubMemberModel? = null,
    val me: ClubMemberModel? = null,
    val dialog: MemberDialog? = null,
    val editShirtNumber: TextFieldState = TextFieldState(),
    val editPosition: PlayerPosition? = null,
    val editError: UiText? = null
) {
    val isSelf: Boolean get() = member != null && member.id == me?.id

    /** APP-RN-05: only the options the backend hierarchy allows for this actor and target. */
    val assignableRoles: List<ClubMemberRole>
        get() = if (member == null || me == null) emptyList()
        else MemberPermissions.assignableRoles(actor = me.role, target = member.role, isSelf = isSelf)
    val canRemove: Boolean get() = member != null && me != null && MemberPermissions.canRemove(me.role, member.role, isSelf)
    val canBan: Boolean get() = member != null && me != null && MemberPermissions.canBan(me.role, member.role, isSelf)
    val canTransfer: Boolean get() = member != null && me != null && MemberPermissions.canTransferOwnership(me.role, isSelf)
}

sealed interface MemberDetailAction {
    data class OnShowDialog(val dialog: MemberDialog) : MemberDetailAction
    data object OnDismissDialog : MemberDetailAction
    data class OnChangeRole(val role: ClubMemberRole) : MemberDetailAction
    data object OnConfirmRemove : MemberDetailAction
    data object OnConfirmBan : MemberDetailAction
    data object OnConfirmTransfer : MemberDetailAction
    data class OnEditPositionSelected(val position: PlayerPosition?) : MemberDetailAction
    data object OnSaveMyMembership : MemberDetailAction
}

sealed interface MemberDetailEvent {
    data class ShowMessage(val message: UiText) : MemberDetailEvent
    /** The member was removed or banned: the profile no longer exists in the club. */
    data object MemberLeftClub : MemberDetailEvent
}
