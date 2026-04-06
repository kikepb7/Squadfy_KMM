package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class UploadClubLogoUseCase(
    private val clubRepository: ClubRepository
) {
    suspend operator fun invoke(bytes: ByteArray, mimeType: String): Result<String, DataError.Remote> =
        clubRepository.uploadClubLogo(bytes = bytes, mimeType = mimeType)
}
