package com.kikepb.club.presentation.memberdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.usecase.CanEditMemberProfileUseCase
import com.kikepb.club.domain.usecase.GetClubMemberByIdUseCase
import com.kikepb.club.domain.usecase.UploadMemberPhotoUseCase
import com.kikepb.club.presentation.memberdetail.MemberDetailAction.OnPhotoSelected
import com.kikepb.club.presentation.memberdetail.MemberDetailAction.OnUploadPhoto
import com.kikepb.club.presentation.memberdetail.MemberDetailEvent.PhotoUpdated
import com.kikepb.club.presentation.memberdetail.MemberDetailEvent.ShowMessage
import com.kikepb.core.presentation.mediapicker.PickedImage
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MemberDetailViewModel(
    private val getClubMemberByIdUseCase: GetClubMemberByIdUseCase,
    private val canEditMemberProfileUseCase: CanEditMemberProfileUseCase,
    private val uploadMemberPhotoUseCase: UploadMemberPhotoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")
    private val memberId = savedStateHandle.get<String>("memberId")
        ?: throw IllegalStateException("memberId is required")

    private val eventChannel = Channel<MemberDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MemberDetailState())

    private val memberWithPermission = getClubMemberByIdUseCase(memberId = memberId)
        .flatMapLatest { member ->
            if (member != null) {
                canEditMemberProfileUseCase(memberUserId = member.userId)
                    .map { canEdit -> Pair(member, canEdit) }
            } else {
                flowOf(Pair(null, false))
            }
        }

    val state = combine(
        flow = _state,
        flow2 = memberWithPermission
    ) { current, (member, canEdit) ->
        current.copy(member = member, isLoading = false, isOwnProfile = canEdit)
    }
        .onStart { _state.update { it.copy(isLoading = true) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = MemberDetailState()
        )

    fun onAction(action: MemberDetailAction) {
        when (action) {
            is OnPhotoSelected -> _state.update {
                it.copy(pendingPhotoSelection = PickedImage(bytes = action.bytes, mimeType = action.mimeType))
            }
            OnUploadPhoto -> uploadPhoto()
        }
    }

    private fun uploadPhoto() {
        val selection = _state.value.pendingPhotoSelection ?: return
        _state.update { it.copy(isUploadingPhoto = true) }
        viewModelScope.launch {
            when (val result = uploadMemberPhotoUseCase(
                clubId = clubId,
                memberId = memberId,
                bytes = selection.bytes,
                mimeType = selection.mimeType
            )) {
                is Success -> {
                    _state.update { it.copy(isUploadingPhoto = false, pendingPhotoSelection = null) }
                    eventChannel.send(PhotoUpdated)
                }
                is Failure -> {
                    _state.update { it.copy(isUploadingPhoto = false) }
                    eventChannel.send(ShowMessage(result.error.toUiText()))
                }
            }
        }
    }
}

data class MemberDetailState(
    val isLoading: Boolean = true,
    val isUploadingPhoto: Boolean = false,
    val isOwnProfile: Boolean = false,
    val member: ClubMemberModel? = null,
    val pendingPhotoSelection: PickedImage? = null
)

sealed interface MemberDetailAction {
    data class OnPhotoSelected(val bytes: ByteArray, val mimeType: String?) : MemberDetailAction
    data object OnUploadPhoto : MemberDetailAction
}

sealed interface MemberDetailEvent {
    data object PhotoUpdated : MemberDetailEvent
    data class ShowMessage(val message: UiText) : MemberDetailEvent
}
