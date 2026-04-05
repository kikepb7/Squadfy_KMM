package com.kikepb.club.domain.repository

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

interface ClubRepository {
    suspend fun getClubById(clubId: String): Result<ClubModel, DataError.Remote>

    suspend fun getClubMembers(clubId: String): Result<List<ClubMemberModel>, DataError.Remote>
}