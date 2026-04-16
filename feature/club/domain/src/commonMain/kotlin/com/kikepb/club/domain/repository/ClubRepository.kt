package com.kikepb.club.domain.repository

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ClubRepository {
    fun getClubById(clubId: String): Flow<ClubModel?>
    fun getClubMembers(clubId: String): Flow<List<ClubMemberModel>>
    suspend fun fetchClubById(clubId: String): EmptyResult<DataError.Remote>
    suspend fun fetchClubMembers(clubId: String): EmptyResult<DataError.Remote>
    suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: String?): Result<ClubModel, DataError.Remote>
    suspend fun createClub(name: String, description: String?, clubLogoUrl: String?, maxMembers: Int?): Result<ClubModel, DataError.Remote>
    suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, DataError.Remote>
}
