package com.kikepb.club.domain.repository

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

interface ClubService {
    suspend fun getClubById(clubId: String): Result<ClubModel, DataError.Remote>
    suspend fun getClubMembers(clubId: String): Result<List<ClubMemberModel>, DataError.Remote>
    suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: String?): Result<ClubModel, DataError.Remote>
    suspend fun createClub(name: String, description: String?, clubLogoUrl: String?, maxMembers: Int?): Result<ClubModel, DataError.Remote>
    suspend fun uploadClubLogo(bytes: ByteArray, mimeType: String): Result<String, DataError.Remote>
}