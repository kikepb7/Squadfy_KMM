package com.kikepb.club.presentation.detail.components

import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.triangulacion.components.PosChip
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriTag
import com.kikepb.core.designsystem.triangulacion.components.TriTagTone
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.presentation.mapper.initialsOf
import com.kikepb.club.presentation.mapper.label
import org.jetbrains.compose.resources.stringResource
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.theme.extended

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadfyClubDetailMemberCard(member: ClubMemberModel, onClick: () -> Unit) {
    // Spec 018: avatar, name with the role when it is not a plain player, shirt number and position chip
    val colors = Triangulacion.colors
    TriCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, contentPadding = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TriAvatar(name = member.username, imageUrl = member.pictureUrl, size = 44.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = member.username, style = Triangulacion.type.title, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    member.shirtNumber?.let { Text(text = "#$it", style = Triangulacion.type.bodySm.tabular, color = colors.inkMuted) }
                    if (member.role != ClubMemberRole.PLAYER) TriTag(text = stringResource(member.role.label), tone = TriTagTone.Neutral)
                }
            }
            member.position.toTri()?.let { PosChip(it) }
        }
    }
}
