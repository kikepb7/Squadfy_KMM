package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.detail.model.ClubDetailTableColumn
import com.kikepb.club.presentation.detail.model.StandingRowUiModel
import com.kikepb.club.presentation.utils.toStandingRow
import com.kikepb.core.designsystem.theme.extended

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
            if (members.isEmpty()) EmptyTabMessage("Aún no hay jugadores en este equipo.")
            else SquadfyClubDetailClassificationTable(members = members, onMemberClick = onMemberClick)
        }
    }
}

@Composable
private fun SquadfyClubDetailClassificationHeader() {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClubDetailTableColumn.entries.forEach { entry ->
            SquadfyClubDetailTableCell(text = entry.title, isHeader = true)
        }
    }
}

@Composable
private fun SquadfyClubDetailClassificationTable(members: List<ClubMemberModel>, onMemberClick: (String) -> Unit) {
    val standings = remember(members) {
        members.map { it.toStandingRow() }
            .sortedWith(compareByDescending<StandingRowUiModel> { it.points }.thenByDescending { it.rating })
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
        ) {
            SquadfyClubDetailClassificationHeader()
            standings.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                SquadfyClubDetailClassificationDataRow(index = index, row = row, onClick = { onMemberClick(row.memberId) })
            }
        }
    }
}

@Composable
private fun SquadfyClubDetailClassificationDataRow(index: Int, row: StandingRowUiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(
                if (index % 2 != 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquadfyClubDetailTableCell(text = row.shirtNumber)
        SquadfyClubDetailTableCell(text = row.playerName)
        SquadfyClubDetailTableCell(text = row.rating)
        SquadfyClubDetailTableCell(text = row.played.toString())
        SquadfyClubDetailTableCell(text = row.wins.toString())
        SquadfyClubDetailTableCell(text = row.draws.toString())
        SquadfyClubDetailTableCell(text = row.losses.toString())
        SquadfyClubDetailTableCell(text = row.goals.toString())
        SquadfyClubDetailTableCell(text = row.minutes.toString())
        SquadfyClubDetailTableCell(text = row.yellow.toString())
        SquadfyClubDetailTableCell(text = row.red.toString())
        SquadfyClubDetailTableCell(text = row.points.toString())
    }
}

@Composable
private fun SquadfyClubDetailTableCell(text: String, isHeader: Boolean = false) {
    Text(
        text = text,
        style = if (isHeader)
            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        else
            MaterialTheme.typography.bodySmall,
        color = if (isHeader) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.extended.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(54.dp)
    )
}