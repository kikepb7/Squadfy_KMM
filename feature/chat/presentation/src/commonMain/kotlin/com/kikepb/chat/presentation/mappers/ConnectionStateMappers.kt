package com.kikepb.chat.presentation.mappers

import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.core.presentation.util.UiText
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.network_error
import squadfy_app.feature.chat.presentation.generated.resources.offline
import squadfy_app.feature.chat.presentation.generated.resources.online
import squadfy_app.feature.chat.presentation.generated.resources.reconnecting
import squadfy_app.feature.chat.presentation.generated.resources.unknown_error

fun ConnectionStateModel.toUiText(): UiText {
    val resource = when (this) {
        ConnectionStateModel.DISCONNECTED -> RString.offline
        ConnectionStateModel.CONNECTING -> RString.reconnecting
        ConnectionStateModel.CONNECTED -> RString.online
        ConnectionStateModel.ERROR_NETWORK -> RString.network_error
        ConnectionStateModel.ERROR_UNKNOWN -> RString.unknown_error
    }

    return UiText.Resource(id = resource)
}