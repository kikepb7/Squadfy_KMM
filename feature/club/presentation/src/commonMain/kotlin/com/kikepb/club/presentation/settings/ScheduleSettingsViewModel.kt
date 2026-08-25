package com.kikepb.club.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubScheduleExceptionModel
import com.kikepb.club.domain.usecase.AddScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.ListScheduleExceptionsUseCase
import com.kikepb.club.domain.usecase.RemoveScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.UpdateClubScheduleUseCase
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleSettingsViewModel(
    private val updateClubScheduleUseCase: UpdateClubScheduleUseCase,
    private val listScheduleExceptionsUseCase: ListScheduleExceptionsUseCase,
    private val addScheduleExceptionUseCase: AddScheduleExceptionUseCase,
    private val removeScheduleExceptionUseCase: RemoveScheduleExceptionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val _state = MutableStateFlow(ScheduleSettingsState())
    val state = _state.asStateFlow()

    init {
        loadExceptions()
    }

    fun saveSchedule(
        matchDayOfWeek: String?,
        matchStartTime: String?,
        matchEndTime: String?,
        seasonStartMonth: Int?,
        seasonStartDay: Int?,
        drawTime: String?
    ) {
        _state.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            when (val result = updateClubScheduleUseCase(
                clubId = clubId,
                matchDayOfWeek = matchDayOfWeek,
                matchStartTime = matchStartTime,
                matchEndTime = matchEndTime,
                seasonStartMonth = seasonStartMonth,
                seasonStartDay = seasonStartDay,
                drawTime = drawTime
            )) {
                is Success -> _state.update { it.copy(isSaving = false, message = UiText.DynamicString("Horario actualizado")) }
                is Failure -> _state.update { it.copy(isSaving = false, message = result.error.toUiText()) }
            }
        }
    }

    fun addException(date: String, reason: String?) {
        if (date.isBlank()) return
        _state.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            when (val result = addScheduleExceptionUseCase(clubId = clubId, date = date, reason = reason)) {
                is Success -> {
                    _state.update { it.copy(isSaving = false) }
                    loadExceptions()
                }
                is Failure -> _state.update { it.copy(isSaving = false, message = result.error.toUiText()) }
            }
        }
    }

    fun removeException(exceptionId: String) {
        viewModelScope.launch {
            when (val result = removeScheduleExceptionUseCase(clubId = clubId, exceptionId = exceptionId)) {
                is Success -> loadExceptions()
                is Failure -> _state.update { it.copy(message = result.error.toUiText()) }
            }
        }
    }

    private fun loadExceptions() {
        viewModelScope.launch {
            when (val result = listScheduleExceptionsUseCase(clubId = clubId)) {
                is Success -> _state.update { it.copy(exceptions = result.data) }
                is Failure -> _state.update { it.copy(message = result.error.toUiText()) }
            }
        }
    }
}

data class ScheduleSettingsState(
    val isSaving: Boolean = false,
    val exceptions: List<ClubScheduleExceptionModel> = emptyList(),
    val message: UiText? = null
)
