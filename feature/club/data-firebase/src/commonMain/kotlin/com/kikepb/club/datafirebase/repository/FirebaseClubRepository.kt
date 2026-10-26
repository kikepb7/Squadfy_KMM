package com.kikepb.club.datafirebase.repository

import com.kikepb.club.datafirebase.dto.ChangeRoleRequest
import com.kikepb.club.datafirebase.dto.ClubDocDto
import com.kikepb.club.datafirebase.dto.ClubIdRequest
import com.kikepb.club.datafirebase.dto.ClubResponseDto
import com.kikepb.club.datafirebase.dto.CreateClubRequest
import com.kikepb.club.datafirebase.dto.InvitationCodeDto
import com.kikepb.club.datafirebase.dto.JoinClubRequest
import com.kikepb.club.datafirebase.dto.MemberDocDto
import com.kikepb.club.datafirebase.dto.MemberRequest
import com.kikepb.club.datafirebase.dto.SetClubLogoRequest
import com.kikepb.club.datafirebase.dto.SetClubPictureRequest
import com.kikepb.club.datafirebase.dto.UpdateClubRequest
import com.kikepb.club.datafirebase.dto.UpdateMembershipRequest
import com.kikepb.club.datafirebase.dto.isActive
import com.kikepb.club.datafirebase.dto.toDomain
import com.kikepb.club.datafirebase.util.MemberIds
import com.kikepb.club.datafirebase.util.asClubResult
import com.kikepb.club.datafirebase.util.asEmptyClubResult
import com.kikepb.club.datafirebase.util.clubRead
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.ClubOperation
import com.kikepb.club.domain.model.ClubBanModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.firebase.firestore.FirestorePaths
import com.kikepb.core.firebase.firestore.clubCollection
import com.kikepb.core.firebase.firestore.clubDocument
import com.kikepb.core.firebase.firestore.toInstant
import com.kikepb.core.firebase.functions.FunctionsClient
import com.kikepb.core.firebase.storage.FirebaseImageStorage
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Clubs and membership on Firebase (spec 019 AC-019-07, AC-019-14). Reads are Firestore listeners, so every screen
 * sees changes made by other members at once (no `fetch` needed; the `fetch*` calls only surface errors such as
 * having been removed). Writes go through the club callables, which apply the backend rules.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseClubRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FunctionsClient,
    private val images: FirebaseImageStorage
) : ClubRepository {

    private val uid: String? get() = auth.currentUser?.uid

    // region Reads

    override fun observeMyClubs(): Flow<List<ClubModel>> =
        observeMyActiveMemberships().flatMapLatest { memberships ->
            if (memberships.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(memberships.map { observeClub(it.clubId) }) { clubs -> clubs.filterNotNull().sortedBy { it.name.lowercase() } }
            }
        }

    override fun getClubById(clubId: String): Flow<ClubModel?> = observeClub(clubId)

    override fun getClubMembers(clubId: String): Flow<List<ClubMemberModel>> =
        firestore.clubCollection(clubId, FirestorePaths.MEMBERS)
            .where { "status" equalTo ACTIVE }
            .snapshots
            .map { snapshot -> snapshot.documents.mapNotNull { it.toMember() } }
            .catch { emit(emptyList()) }

    override fun getClubMemberById(memberId: String): Flow<ClubMemberModel?> {
        val clubId = MemberIds.clubOf(memberId) ?: return flowOf(null)
        return firestore.clubCollection(clubId, FirestorePaths.MEMBERS).document(memberId)
            .snapshots
            .map { it.toMember() }
            .catch { emit(null) }
    }

    override fun observeMembershipsOfUser(userId: String): Flow<List<ClubMemberModel>> =
        firestore.collectionGroup(FirestorePaths.MEMBERS)
            .where { ("userId" equalTo userId) and ("status" equalTo ACTIVE) }
            .snapshots
            .map { snapshot -> snapshot.documents.mapNotNull { it.toMember() } }
            .catch { emit(emptyList()) }

    private fun observeMyActiveMemberships(): Flow<List<ClubMemberModel>> =
        auth.authStateChanged
            .onStart { emit(auth.currentUser) }
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else observeMembershipsOfUser(uid) }

    private fun observeClub(clubId: String): Flow<ClubModel?> =
        firestore.clubDocument(clubId).snapshots
            .map { snapshot -> if (snapshot.exists) snapshot.data(ClubDocDto.serializer()).toDomain(snapshot.id) else null }
            // Denied once the user leaves or is removed: the club simply disappears
            .catch { emit(null) }

    private fun DocumentSnapshot.toMember(): ClubMemberModel? =
        if (exists) data(MemberDocDto.serializer()).takeIf { it.isActive }?.toDomain(id) else null

    // endregion

    // region Sync: listeners keep everything fresh; these only report access errors

    override suspend fun fetchMyClubs(): EmptyResult<ClubError> = Result.Success(Unit)

    override suspend fun fetchClubById(clubId: String): EmptyResult<ClubError> =
        clubRead { firestore.clubDocument(clubId).get() }.map { }

    override suspend fun fetchClubMembers(clubId: String): EmptyResult<ClubError> =
        clubRead { firestore.clubCollection(clubId, FirestorePaths.MEMBERS).where { "status" equalTo ACTIVE }.get() }.map { }

    // endregion

    // region Clubs

    override suspend fun createClub(name: String, description: String?, maxMembers: Int?): Result<ClubModel, ClubError> =
        functions.call<CreateClubRequest, ClubResponseDto>("createClub", CreateClubRequest(name, description, maxMembers))
            .asClubResult()
            .map { it.toDomain() }

    override suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubModel, ClubError> =
        functions.call<JoinClubRequest, ClubResponseDto>("joinClub", JoinClubRequest(invitationCode, shirtNumber, position?.name))
            .asClubResult(ClubOperation.JOIN)
            .map { it.toDomain() }

    override suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, ClubError> {
        val url = when (val uploaded = images.upload(FirebaseImageStorage.clubLogo(clubId), bytes, mimeType)) {
            is Result.Success -> uploaded.data
            is Result.Failure -> return Result.Failure(ClubError.Remote(RemoteError(status = uploaded.error)))
        }
        return functions.call<SetClubLogoRequest, ClubResponseDto>("setClubLogo", SetClubLogoRequest(clubId, url))
            .asClubResult()
            .map { it.toDomain() }
    }

    override suspend fun editClub(clubId: String, name: String?, description: String?, maxMembers: Int?): Result<ClubModel, ClubError> =
        functions.call<UpdateClubRequest, ClubResponseDto>("updateClub", UpdateClubRequest(clubId, name, description, maxMembers))
            .asClubResult(ClubOperation.EDIT_CLUB)
            .map { it.toDomain() }

    override suspend fun regenerateInvitationCode(clubId: String): Result<String, ClubError> =
        functions.call<ClubIdRequest, InvitationCodeDto>("regenerateInvitationCode", ClubIdRequest(clubId))
            .asClubResult()
            .map { it.invitationCode }

    override suspend fun transferOwnership(clubId: String, memberId: String): Result<ClubModel, ClubError> =
        functions.call<MemberRequest, ClubResponseDto>("transferOwnership", MemberRequest(clubId, memberId))
            .asClubResult()
            .map { it.toDomain() }

    // endregion

    // region Members

    override suspend fun updateMyMembership(clubId: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubMemberModel, ClubError> =
        functions.call<UpdateMembershipRequest, MemberDocDto>("updateMyMembership", UpdateMembershipRequest(clubId, shirtNumber, position?.name))
            .asClubResult()
            .map { it.toDomain(it.id.orEmpty()) }

    override suspend fun uploadMyClubPicture(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubMemberModel, ClubError> {
        val me = uid ?: return Result.Failure(ClubError.Remote(RemoteError(status = DataError.Remote.UNAUTHORIZED)))
        val url = when (val uploaded = images.upload(FirebaseImageStorage.clubMemberPicture(clubId, me), bytes, mimeType)) {
            is Result.Success -> uploaded.data
            is Result.Failure -> return Result.Failure(ClubError.Remote(RemoteError(status = uploaded.error)))
        }
        return setMyClubPicture(clubId, url)
    }

    override suspend fun removeMyClubPicture(clubId: String): Result<ClubMemberModel, ClubError> {
        val result = setMyClubPicture(clubId, null)
        uid?.let { images.delete(FirebaseImageStorage.clubMemberPicture(clubId, it)) }
        return result
    }

    private suspend fun setMyClubPicture(clubId: String, url: String?): Result<ClubMemberModel, ClubError> =
        functions.call<SetClubPictureRequest, MemberDocDto>("setMyClubPicture", SetClubPictureRequest(clubId, url))
            .asClubResult()
            .map { it.toDomain(it.id.orEmpty()) }

    override suspend fun leaveClub(clubId: String): EmptyResult<ClubError> =
        functions.run("leaveClub", ClubIdRequest(clubId)).asEmptyClubResult()

    override suspend fun removeMember(clubId: String, memberId: String): EmptyResult<ClubError> =
        functions.run("removeMember", MemberRequest(clubId, memberId)).asEmptyClubResult()

    override suspend fun changeMemberRole(clubId: String, memberId: String, role: ClubMemberRole): Result<ClubMemberModel, ClubError> =
        functions.call<ChangeRoleRequest, MemberDocDto>("changeMemberRole", ChangeRoleRequest(clubId, memberId, role.name))
            .asClubResult()
            .map { it.toDomain(it.id.orEmpty()) }

    override suspend fun getBans(clubId: String): Result<List<ClubBanModel>, ClubError> =
        clubRead {
            firestore.clubCollection(clubId, FirestorePaths.MEMBERS).where { "status" equalTo BANNED }.get().documents
        }.map { documents ->
            documents.map { snapshot ->
                val member = snapshot.data(MemberDocDto.serializer())
                ClubBanModel(
                    clubMemberId = snapshot.id,
                    userId = member.userId,
                    username = member.username,
                    bannedAt = member.bannedAt?.toInstant()?.toString().orEmpty()
                )
            }
        }

    override suspend fun banMember(clubId: String, memberId: String): EmptyResult<ClubError> =
        functions.run("banMember", MemberRequest(clubId, memberId)).asEmptyClubResult()

    override suspend fun unbanMember(clubId: String, memberId: String): EmptyResult<ClubError> =
        functions.run("unbanMember", MemberRequest(clubId, memberId)).asEmptyClubResult()

    // endregion

    private companion object {
        const val ACTIVE = "ACTIVE"
        const val BANNED = "BANNED"
    }
}
