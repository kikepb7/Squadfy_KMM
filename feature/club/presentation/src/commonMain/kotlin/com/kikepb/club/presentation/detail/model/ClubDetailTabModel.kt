package com.kikepb.club.presentation.detail.model

import org.jetbrains.compose.resources.StringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_tab_classification
import squadfy_app.feature.club.presentation.generated.resources.club_tab_match
import squadfy_app.feature.club.presentation.generated.resources.club_tab_members
import squadfy_app.feature.club.presentation.generated.resources.club_tab_settings

/** The match tab comes first: it is what members open every week (AC-005-01). */
enum class ClubDetailTabModel(val title: StringResource) {
    MATCH(title = Res.string.club_tab_match),
    CLASSIFICATION(title = Res.string.club_tab_classification),
    MEMBERS(title = Res.string.club_tab_members),
    SETTINGS(title = Res.string.club_tab_settings)
}
