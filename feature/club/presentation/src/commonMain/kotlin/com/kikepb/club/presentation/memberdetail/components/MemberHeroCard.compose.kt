package com.kikepb.club.presentation.memberdetail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.presentation.utils.initialsOf
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.theme.SquadfyBase0
import com.kikepb.core.designsystem.theme.SquadfyBase900
import com.kikepb.core.designsystem.theme.extended

@Composable
fun MemberHeroCard(
    member: ClubMemberModel,
    isOwnProfile: Boolean,
    pendingPhotoBytes: ByteArray?,
    isUploadingPhoto: Boolean,
    onPickPhoto: () -> Unit,
    onUploadPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initials = remember(member.username) { initialsOf(member.username) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SquadfyBase900)
                            .then(
                                if (isOwnProfile) Modifier.clickable(onClick = onPickPhoto)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pendingPhotoBytes != null) {
                            AsyncImage(
                                model = pendingPhotoBytes,
                                contentDescription = "Vista previa de nueva foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else if (member.profilePictureUrl != null) {
                            AsyncImage(
                                model = member.profilePictureUrl,
                                contentDescription = "Foto de ${member.username}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = if (member.shirtNumber != null) "#${member.shirtNumber}"
                                       else initials,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                ),
                                color = SquadfyBase0
                            )
                        }

                        if (isUploadingPhoto) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (isOwnProfile) {
                        FilledIconButton(
                            onClick = onPickPhoto,
                            modifier = Modifier.size(26.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Cambiar foto de perfil",
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val label = buildList {
                        if (member.shirtNumber != null) add("#${member.shirtNumber}")
                        if (!member.position.isNullOrBlank()) add(member.position!!.uppercase())
                    }.joinToString(" · ")

                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.extended.textPlaceholder
                        )
                    }

                    Text(
                        text = member.username,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.extended.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val roleLabel = when (member.role.uppercase()) {
                        "OWNER" -> "Propietario"
                        "ADMIN" -> "Admin"
                        else -> "Jugador"
                    }
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.extended.surfaceOutline,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.extended.textSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isOwnProfile && pendingPhotoBytes != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.extended.surfaceOutline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SquadfyButton(
                        text = "Guardar foto",
                        onClick = onUploadPhoto,
                        isLoading = isUploadingPhoto,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
