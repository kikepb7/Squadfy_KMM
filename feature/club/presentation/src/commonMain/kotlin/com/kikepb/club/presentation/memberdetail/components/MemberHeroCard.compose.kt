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
import androidx.compose.ui.graphics.Brush
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
import com.kikepb.core.designsystem.theme.SquadfyBrand500
import com.kikepb.core.designsystem.theme.SquadfyBrand600
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(size = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                SquadfyBrand600.copy(alpha = 0.85f),
                                SquadfyBrand500.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                MemberAvatar(
                    member = member,
                    isOwnProfile = isOwnProfile,
                    pendingPhotoBytes = pendingPhotoBytes,
                    isUploading = isUploadingPhoto,
                    onPickPhoto = onPickPhoto
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = member.username,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (member.position != null) {
                        MemberChip(
                            text = member.position.toString(),
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (member.shirtNumber != null) {
                        MemberChip(
                            text = "#${member.shirtNumber}",
                            containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
                            textColor = MaterialTheme.colorScheme.extended.textSecondary
                        )
                    }
                    MemberChip(
                        text = member.role.replaceFirstChar { it.uppercase() },
                        containerColor = SquadfyBrand500.copy(alpha = 0.15f),
                        textColor = SquadfyBrand600
                    )
                }

                AnimatedVisibility(
                    visible = isOwnProfile && pendingPhotoBytes != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(14.dp))
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
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberAvatar(
    member: ClubMemberModel,
    isOwnProfile: Boolean,
    pendingPhotoBytes: ByteArray?,
    isUploading: Boolean,
    onPickPhoto: () -> Unit
) {
    val initials = remember(member.username) { initialsOf(member.username) }

    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .then(
                    if (isOwnProfile) Modifier.clickable(onClick = onPickPhoto) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

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
                    contentDescription = "Foto de perfil de ${member.username}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                }
            }
        }

        if (isOwnProfile) {
            FilledIconButton(
                onClick = onPickPhoto,
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Cambiar foto de perfil",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun MemberChip(
    text: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}
