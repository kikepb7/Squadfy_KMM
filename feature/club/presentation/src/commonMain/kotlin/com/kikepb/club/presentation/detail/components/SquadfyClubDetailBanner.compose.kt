package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.utils.initialsOf
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.theme.SquadfyBrand1000
import com.kikepb.core.designsystem.theme.SquadfyBrand600
import com.kikepb.core.designsystem.theme.SquadfyBrand900

@Composable
fun SquadfyClubDetailBanner(club: ClubModel, modifier: Modifier = Modifier) {
    val initials = remember(club.name) { initialsOf(club.name) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SquadfyBrand1000, SquadfyBrand900, SquadfyBrand600)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SquadfyAvatarPhoto(
                        displayText = initials,
                        imageUrl = club.clubLogoUrl,
                        size = AvatarSize.LARGE,
                        textColor = Color.White
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = club.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!club.description.isNullOrBlank()) {
                            Text(
                                text = club.description.orEmpty(),
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SquadfyClubDetailBadge(
                        icon = Icons.Outlined.Person,
                        text = "${club.membersCount}${club.maxMembers?.let { "/$it" } ?: ""} miembros"
                    )
                    SquadfyClubDetailBadge(
                        icon = Icons.Outlined.Info,
                        text = club.invitationCode
                    )
                }
            }
        }
    }
}

@Composable
private fun SquadfyClubDetailBadge(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            tint = Color.White.copy(alpha = 0.85f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}