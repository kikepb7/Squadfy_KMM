package com.kikepb.club.presentation.mapper

import com.kikepb.club.domain.model.CreateClubError
import com.kikepb.club.domain.model.CreateClubError.BlankName
import com.kikepb.club.domain.model.CreateClubError.InvalidMaxMembers
import com.kikepb.club.domain.model.CreateClubError.NameTooLong
import com.kikepb.club.domain.model.JoinClubError
import com.kikepb.club.domain.model.JoinClubError.InvalidInvitationCodeFormat
import com.kikepb.club.domain.model.JoinClubError.InvalidShirtNumber
import com.kikepb.club.domain.model.JoinClubError.Remote
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import com.kikepb.core.presentation.util.UiText.DynamicString

fun CreateClubError.toUiText(): UiText = when (this) {
    BlankName -> DynamicString("El nombre del club es obligatorio")
    NameTooLong -> DynamicString("El nombre no puede superar los 120 caracteres")
    InvalidMaxMembers -> DynamicString("Introduce un número entero positivo")
    is CreateClubError.Remote -> dataError.toUiText()
}

fun JoinClubError.toUiText(): UiText = when (this) {
    InvalidInvitationCodeFormat -> DynamicString("El código debe tener entre 6 y 12 caracteres alfanuméricos")
    InvalidShirtNumber -> DynamicString("El dorsal debe estar entre 1 y 100")
    is Remote -> dataError.toUiText()
}
