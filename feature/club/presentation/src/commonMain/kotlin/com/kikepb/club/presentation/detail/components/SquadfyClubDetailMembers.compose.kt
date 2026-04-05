package com.kikepb.club.presentation.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.core.designsystem.theme.extended
import kotlinx.coroutines.delay

@Composable
fun SquadfyClubDetailMembersTab(members: List<ClubMemberModel>, onMemberClick: (String) -> Unit) {
    if (members.isEmpty()) {
        EmptyTabMessage(
            text = "Aún no hay miembros en este equipo.",
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "${members.size} miembro${if (members.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.extended.textPlaceholder,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        itemsIndexed(items = members, key = { _, m -> m.id }) { index, member ->
            SquadfyClubDetailStaggeredMemberItem(member = member, index = index, onClick = { onMemberClick(member.id) })
        }
    }
}

@Composable
private fun SquadfyClubDetailStaggeredMemberItem(member: ClubMemberModel, index: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(member.id) {
        delay(index * 55L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + slideInVertically(tween(360)) { 48 }
    ) {
        SquadfyClubDetailMemberCard(member = member, onClick = onClick)
    }
}