package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class UploadMemberPhotoUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(
        clubId: String,
        memberId: String,
        bytes: ByteArray,
        mimeType: String?
    ): Result<ClubMemberModel, DataError.Remote> =
        clubRepository.uploadMemberPhoto(
            clubId = clubId,
            memberId = memberId,
            bytes = bytes,
            mimeType = mimeType ?: DEFAULT_MIME_TYPE
        )

    private companion object {
        const val DEFAULT_MIME_TYPE = "image/jpeg"
    }
}
