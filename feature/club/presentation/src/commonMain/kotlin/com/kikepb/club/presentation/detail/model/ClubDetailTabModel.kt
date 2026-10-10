package com.kikepb.club.presentation.detail.model

import org.jetbrains.compose.resources.StringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_settings_title
import squadfy_app.feature.club.presentation.generated.resources.club_tab_match
import squadfy_app.feature.club.presentation.generated.resources.club_tab_members
import squadfy_app.feature.club.presentation.generated.resources.club_tab_table
import squadfy_app.feature.club.presentation.generated.resources.club_tab_teams

/**
 * Tabs of the club (spec 018): Partido, Equipos, Tabla, Miembros. The match tab comes first: it is what members
 * open every week (AC-005-01). Settings are not a tab any more: the header button opens them.
 */
enum class ClubDetailTabModel(val title: StringResource, val isTab: Boolean = true) {
    MATCH(title = Res.string.club_tab_match),
    TEAMS(title = Res.string.club_tab_teams),
    CLASSIFICATION(title = Res.string.club_tab_table),
    MEMBERS(title = Res.string.club_tab_members),
    SETTINGS(title = Res.string.club_settings_title, isTab = false);

    companion object {
        val tabs: List<ClubDetailTabModel> = entries.filter { it.isTab }
    }
}
