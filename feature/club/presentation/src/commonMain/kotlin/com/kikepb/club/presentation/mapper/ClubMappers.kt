package com.kikepb.club.presentation.mapper

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.CreateClubError
import com.kikepb.club.domain.model.EditClubError
import com.kikepb.club.domain.model.JoinClubError
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import org.jetbrains.compose.resources.StringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_error_already_member_or_full
import squadfy_app.feature.club.presentation.generated.resources.club_error_banned
import squadfy_app.feature.club.presentation.generated.resources.club_error_blank_name
import squadfy_app.feature.club.presentation.generated.resources.club_error_code_format
import squadfy_app.feature.club.presentation.generated.resources.club_error_description_too_long
import squadfy_app.feature.club.presentation.generated.resources.club_error_forbidden
import squadfy_app.feature.club.presentation.generated.resources.club_error_invalid_code
import squadfy_app.feature.club.presentation.generated.resources.club_error_invalid_max_members
import squadfy_app.feature.club.presentation.generated.resources.club_error_max_members_below
import squadfy_app.feature.club.presentation.generated.resources.club_error_name_too_long
import squadfy_app.feature.club.presentation.generated.resources.club_error_not_found
import squadfy_app.feature.club.presentation.generated.resources.club_error_not_member
import squadfy_app.feature.club.presentation.generated.resources.club_error_shirt_number
import squadfy_app.feature.club.presentation.generated.resources.position_defender
import squadfy_app.feature.club.presentation.generated.resources.position_forward
import squadfy_app.feature.club.presentation.generated.resources.position_goalkeeper
import squadfy_app.feature.club.presentation.generated.resources.position_midfielder
import squadfy_app.feature.club.presentation.generated.resources.position_none
import squadfy_app.feature.club.presentation.generated.resources.role_admin
import squadfy_app.feature.club.presentation.generated.resources.role_captain
import squadfy_app.feature.club.presentation.generated.resources.role_owner
import squadfy_app.feature.club.presentation.generated.resources.role_player

/** APP-RN-07: texts depend on the typed error, never on the backend message. */
fun ClubError.toUiText(): UiText = when (this) {
    ClubError.InvalidInvitationCode -> UiText.Resource(Res.string.club_error_invalid_code)
    ClubError.BannedFromClub -> UiText.Resource(Res.string.club_error_banned)
    ClubError.AlreadyMemberOrClubFull -> UiText.Resource(Res.string.club_error_already_member_or_full)
    ClubError.NotClubMember -> UiText.Resource(Res.string.club_error_not_member)
    ClubError.Forbidden -> UiText.Resource(Res.string.club_error_forbidden)
    ClubError.MaxMembersBelowCurrent -> UiText.Resource(Res.string.club_error_max_members_below)
    ClubError.NotFound -> UiText.Resource(Res.string.club_error_not_found)
    is ClubError.Remote -> error.toUiText()
}

fun CreateClubError.toUiText(): UiText = when (this) {
    CreateClubError.BlankName -> UiText.Resource(Res.string.club_error_blank_name)
    CreateClubError.NameTooLong -> UiText.Resource(Res.string.club_error_name_too_long)
    CreateClubError.DescriptionTooLong -> UiText.Resource(Res.string.club_error_description_too_long)
    CreateClubError.InvalidMaxMembers -> UiText.Resource(Res.string.club_error_invalid_max_members)
    is CreateClubError.Remote -> error.toUiText()
}

fun EditClubError.toUiText(): UiText = when (this) {
    EditClubError.BlankName -> UiText.Resource(Res.string.club_error_blank_name)
    EditClubError.NameTooLong -> UiText.Resource(Res.string.club_error_name_too_long)
    EditClubError.DescriptionTooLong -> UiText.Resource(Res.string.club_error_description_too_long)
    EditClubError.InvalidMaxMembers -> UiText.Resource(Res.string.club_error_invalid_max_members)
    is EditClubError.Remote -> error.toUiText()
}

fun JoinClubError.toUiText(): UiText = when (this) {
    JoinClubError.InvalidInvitationCodeFormat -> UiText.Resource(Res.string.club_error_code_format)
    JoinClubError.InvalidShirtNumber -> UiText.Resource(Res.string.club_error_shirt_number)
    is JoinClubError.Remote -> error.toUiText()
}

val ClubMemberRole.label: StringResource
    get() = when (this) {
        ClubMemberRole.OWNER -> Res.string.role_owner
        ClubMemberRole.ADMIN -> Res.string.role_admin
        ClubMemberRole.CAPTAIN -> Res.string.role_captain
        ClubMemberRole.PLAYER -> Res.string.role_player
    }

val PlayerPosition?.label: StringResource
    get() = when (this) {
        PlayerPosition.GOALKEEPER -> Res.string.position_goalkeeper
        PlayerPosition.DEFENDER -> Res.string.position_defender
        PlayerPosition.MIDFIELDER -> Res.string.position_midfielder
        PlayerPosition.FORWARD -> Res.string.position_forward
        null -> Res.string.position_none
    }

/** Two-letter initials for avatars without a picture. */
fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
