package com.kikepb.core.firebase.storage

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.firebase.error.safeFirebaseCall
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storageMetadata

/**
 * Images in Cloud Storage (spec 019 AC-019-17): profile pictures, club logos and per-club photos. The paths and the
 * 5 MB / image-only limits are enforced again by `firebase/storage.rules`.
 */
class FirebaseImageStorage(
    private val storage: Lazy<FirebaseStorage>
) {

    /** Uploads [bytes] to [path] and returns its download URL, cache-busted so the new picture shows at once. */
    suspend fun upload(path: String, bytes: ByteArray, mimeType: String): Result<String, DataError.Remote> {
        if (bytes.size > MAX_IMAGE_BYTES) return Result.Failure(DataError.Remote.PAYLOAD_TOO_LARGE)
        if (!mimeType.startsWith("image/")) return Result.Failure(DataError.Remote.BAD_REQUEST)
        return safeFirebaseCall {
            val reference = storage.value.reference(path)
            reference.putData(bytes.toStorageData(), storageMetadata { contentType = mimeType })
            reference.getDownloadUrl()
        }
    }

    suspend fun delete(path: String): EmptyResult<DataError.Remote> =
        safeFirebaseCall { storage.value.reference(path).delete() }

    companion object {
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024

        fun userAvatar(uid: String) = "users/$uid/avatar.jpg"
        fun clubLogo(clubId: String) = "clubs/$clubId/logo.jpg"
        fun clubMemberPicture(clubId: String, uid: String) = "clubs/$clubId/members/$uid.jpg"
    }
}

internal expect fun ByteArray.toStorageData(): Data
