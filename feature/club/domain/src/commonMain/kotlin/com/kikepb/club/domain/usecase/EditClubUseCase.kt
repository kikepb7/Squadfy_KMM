package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.EditClubError
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.mapError

/** BE-001 RN-13. maxMembers below the current members is a business 400 (AC-003-11). */
class EditClubUseCase(private val clubRepository: ClubRepository) {

    suspend operator fun invoke(clubId: String, name: String, description: String?, maxMembersRaw: String?): Result<ClubModel, EditClubError> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return Failure(EditClubError.BlankName)
        if (trimmedName.length > ClubValidation.MAX_NAME_LENGTH) return Failure(EditClubError.NameTooLong)
        if ((description?.length ?: 0) > ClubValidation.MAX_DESCRIPTION_LENGTH) return Failure(EditClubError.DescriptionTooLong)
        val maxMembers = when {
            maxMembersRaw.isNullOrBlank() -> null
            else -> maxMembersRaw.trim().toIntOrNull()?.takeIf { it > 0 } ?: return Failure(EditClubError.InvalidMaxMembers)
        }
        return clubRepository.editClub(clubId = clubId, name = trimmedName, description = description?.trim(), maxMembers = maxMembers)
            .mapError { EditClubError.Remote(it) }
    }
}
