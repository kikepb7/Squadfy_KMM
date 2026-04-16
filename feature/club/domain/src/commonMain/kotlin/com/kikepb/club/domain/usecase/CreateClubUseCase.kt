package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.CreateClubError
import com.kikepb.club.domain.model.CreateClubError.BlankName
import com.kikepb.club.domain.model.CreateClubError.NameTooLong
import com.kikepb.club.domain.model.CreateClubError.Remote
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success

class CreateClubUseCase(private val clubRepository: ClubRepository) {

    suspend operator fun invoke(
        name: String,
        description: String?,
        maxMembersRaw: String?,
        logoBytes: ByteArray?,
        logoMimeType: String?
    ): Result<ClubModel, CreateClubError> {

        if (name.isBlank()) return Failure(error = BlankName)
        if (name.length > MAX_NAME_LENGTH) return Failure(error = NameTooLong)

        val maxMembers = when (val result = parseMaxMembers(maxMembersRaw)) {
            is Success -> result.data
            is Failure -> return Failure(error = result.error)
        }

        val club = when (val result = clubRepository.createClub(
            name = name,
            description = description,
            clubLogoUrl = null,
            maxMembers = maxMembers
        )) {
            is Success -> result.data
            is Failure -> return Failure(error = Remote(dataError = result.error))
        }

        if (logoBytes != null && logoMimeType != null) {
            return when (val result = clubRepository.uploadClubLogo(
                clubId = club.id,
                bytes = logoBytes,
                mimeType = logoMimeType
            )) {
                is Success -> Success(data = result.data)
                is Failure -> Failure(error = Remote(dataError = result.error))
            }
        }

        return Success(club)
    }

    private fun parseMaxMembers(raw: String?): Result<Int?, CreateClubError> {
        if (raw.isNullOrBlank()) return Success(null)
        val n = raw.trim().toIntOrNull()
        return if (n != null && n >= MIN_MEMBERS) Success(n)
        else Failure(CreateClubError.InvalidMaxMembers)
    }

    companion object {
        private const val MAX_NAME_LENGTH = 120
        private const val MIN_MEMBERS = 1
    }
}
