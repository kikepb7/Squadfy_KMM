package com.kikepb.club.data.datasource.remote

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.repository.NotificationSettingsRepository
import com.kikepb.core.data.networking.apiGet
import com.kikepb.core.data.networking.apiPut
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.mapError
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable

/** `ClubNotificationSettingsDto`. */
@Serializable
data class ClubNotificationSettingsDTO(val clubId: String? = null, val muted: Boolean = false)

@Serializable
data class UpdateClubNotificationSettingsRequestDTO(val muted: Boolean)

class KtorNotificationSettingsRepository(private val httpClient: HttpClient) : NotificationSettingsRepository {

    override suspend fun isMuted(clubId: String): Result<Boolean, ClubError> =
        httpClient.apiGet<ClubNotificationSettingsDTO>(route = route(clubId)).mapError { it.toClubError() }.map { it.muted }

    override suspend fun setMuted(clubId: String, muted: Boolean): Result<Boolean, ClubError> =
        httpClient.apiPut<UpdateClubNotificationSettingsRequestDTO, ClubNotificationSettingsDTO>(
            route = route(clubId),
            body = UpdateClubNotificationSettingsRequestDTO(muted)
        ).mapError { it.toClubError() }.map { it.muted }

    private fun route(clubId: String) = "/clubs/$clubId/notification-settings"
}
