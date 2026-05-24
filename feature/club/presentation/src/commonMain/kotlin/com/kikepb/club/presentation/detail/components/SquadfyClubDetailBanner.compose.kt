package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.utils.initialsOf
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.theme.SquadfyBrand1000
import com.kikepb.core.designsystem.theme.SquadfyBrand500
import com.kikepb.core.designsystem.theme.SquadfyBrand600
import com.kikepb.core.designsystem.theme.SquadfyBrand900
import com.kikepb.core.designsystem.theme.SquadfyRed500

@Composable
fun SquadfyClubDetailBanner(club: ClubModel, modifier: Modifier = Modifier) {
    val initials = remember(club.name) { initialsOf(club.name) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SquadfyBrand1000, SquadfyBrand900, SquadfyBrand600)
                )
            )
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SquadfyAvatarPhoto(
            displayText = initials,
            imageUrl = club.clubLogoUrl,
            size = AvatarSize.LARGE,
            textColor = Color.White
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = club.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.5).sp
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = SquadfyRed500
            )
            Text(
                text = buildString {
                    if (!club.location.isNullOrBlank()) {
                        append(club.location)
                        append(" · ")
                    }
                    append("Temporada 2025/26")
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ClubBannerBadge(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = SquadfyBrand500
                    )
                },
                text = "${club.membersCount}${club.maxMembers?.let { "/$it" } ?: ""} jugadores",
                outlined = true
            )
        }
    }
}

@Composable
private fun ClubBannerBadge(
    icon: @Composable () -> Unit,
    text: String,
    outlined: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (outlined) SquadfyBrand500.copy(alpha = 0.15f)
                else Color.White.copy(alpha = 0.12f)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (outlined) SquadfyBrand500 else Color.White.copy(alpha = 0.9f)
        )
    }
}