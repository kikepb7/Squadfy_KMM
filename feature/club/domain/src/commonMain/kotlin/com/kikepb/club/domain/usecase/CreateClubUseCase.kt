package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.CreateClubError
import com.kikepb.club.domain.model.CreatedClub
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success

/** BE-001 validation (name ≤ 120, description ≤ 2000, maxMembers > 0) plus the optional logo (AC-003-03). */
class CreateClubUseCase(private val clubRepository: ClubRepository) {

    suspend operator fun invoke(
        name: String,
        description: String?,
        maxMembersRaw: String?,
        logoBytes: ByteArray?,
        logoMimeType: String?
    ): Result<CreatedClub, CreateClubError> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return Failure(CreateClubError.BlankName)
        if (trimmedName.length > ClubValidation.MAX_NAME_LENGTH) return Failure(CreateClubError.NameTooLong)
        if ((description?.length ?: 0) > ClubValidation.MAX_DESCRIPTION_LENGTH) return Failure(CreateClubError.DescriptionTooLong)
        val maxMembers = when {
            maxMembersRaw.isNullOrBlank() -> null
            else -> maxMembersRaw.trim().toIntOrNull()?.takeIf { it > 0 } ?: return Failure(CreateClubError.InvalidMaxMembers)
        }

        val club = when (val result = clubRepository.createClub(
            name = trimmedName,
            description = description?.trim()?.ifBlank { null },
            maxMembers = maxMembers
        )) {
            is Success -> result.data
            is Failure -> return Failure(CreateClubError.Remote(result.error))
        }

        if (logoBytes == null || logoMimeType == null) return Success(CreatedClub(club = club, logoUploadFailed = false))

        // The club already exists: a failed logo must not make the user retry (and duplicate) the creation
        return when (val logo = clubRepository.uploadClubLogo(clubId = club.id, bytes = logoBytes, mimeType = logoMimeType)) {
            is Success -> Success(CreatedClub(club = logo.data, logoUploadFailed = false))
            is Failure -> Success(CreatedClub(club = club, logoUploadFailed = true))
        }
    }
}

object ClubValidation {
    const val MAX_NAME_LENGTH = 120
    const val MAX_DESCRIPTION_LENGTH = 2000
}
