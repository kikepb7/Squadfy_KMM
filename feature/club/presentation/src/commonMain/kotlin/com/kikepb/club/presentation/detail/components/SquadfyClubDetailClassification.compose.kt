package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.mapper.label
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.classification_header_number
import squadfy_app.feature.club.presentation.generated.resources.classification_header_player
import squadfy_app.feature.club.presentation.generated.resources.classification_header_position
import squadfy_app.feature.club.presentation.generated.resources.classification_title
import squadfy_app.feature.club.presentation.generated.resources.club_members_empty

/**
 * Squad overview. The rating and stats standings replace this table in spec 008; the client-side
 * "performance index" built on always-zero member stats is gone (AC-008-07).
 */
@Composable
fun SquadfyClubDetailClassificationTab(
    club: ClubModel,
    members: List<ClubMemberModel>,
    onMemberClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SquadfyClubDetailIdentityCard(club = club) }
        item {
            Text(
                text = stringResource(Res.string.classification_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
        }
        item {
            if (members.isEmpty()) EmptyTabMessage(stringResource(Res.string.club_members_empty))
            else SquadTable(members = members.sortedWith(compareBy({ it.shirtNumber ?: Int.MAX_VALUE }, { it.username })), onMemberClick = onMemberClick)
        }
    }
}

@Composable
private fun SquadTable(members: List<ClubMemberModel>, onMemberClick: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCell(text = stringResource(Res.string.classification_header_number), modifier = Modifier.width(40.dp))
                HeaderCell(text = stringResource(Res.string.classification_header_player), modifier = Modifier.weight(1f))
                HeaderCell(text = stringResource(Res.string.classification_header_position), modifier = Modifier.width(120.dp))
            }
            members.forEachIndexed { index, member ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMemberClick(member.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BodyCell(text = member.shirtNumber?.toString() ?: "-", modifier = Modifier.width(40.dp))
                    BodyCell(text = member.username, modifier = Modifier.weight(1f))
                    BodyCell(text = stringResource(member.position.label), modifier = Modifier.width(120.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun BodyCell(text: String, modifier: Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPrimary, modifier = modifier)
}
