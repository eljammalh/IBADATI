package com.ibadati.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.Athkar

@Composable
fun AthkarScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val day = state.day
    var duaOpen by remember { mutableStateOf(false) }
    val goal = state.settings.duaGoal

    ScreenColumn {
        DateNavigator(state.selected, state.today, state.settings.showHijri, state.records::hasAnyData, vm::selectDay)

        HeroCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = day.athkarCount / 7f,
                    modifier = Modifier.size(92.dp),
                    color = Color(0xFFE6C27A),
                    track = Color.White.copy(alpha = 0.15f),
                    stroke = 9.dp,
                ) {
                    Text(L.digits("${day.athkarCount}/7"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(L.athkarCompleted, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${L.dua}: ${L.minutes(day.duaMinutes)}",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(L.tapToMark, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Seven athkar in a two-column grid, in the requested order.
        Athkar.items.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { a ->
                    AthkarTile(
                        name = L.athkarName(a),
                        hint = L.athkarHint(a),
                        done = day.athkarDone(a.index),
                        modifier = Modifier.weight(1f),
                    ) { vm.toggleAthkar(a.index) }
                }
                if (pair.size == 1) {
                    DuaTile(day.duaMinutes, goal, Modifier.weight(1f)) { duaOpen = true }
                }
            }
        }
    }

    if (duaOpen) {
        DuaDialog(initial = day.duaMinutes, onDismiss = { duaOpen = false }) {
            vm.setDua(it)
            duaOpen = false
        }
    }
}

@Composable
private fun AthkarTile(name: String, hint: String, done: Boolean, modifier: Modifier, onTap: () -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    Card(
        modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (done) cs.primaryContainer.copy(alpha = 0.55f) else cs.surfaceContainer),
        border = BorderStroke(1.dp, if (done) cs.primary.copy(alpha = 0.5f) else cs.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).clickable(onClick = onTap).padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WorshipCircle(filled = done, size = 70.dp, label = if (done) "✓" else "", labelSize = 28.sp, onTap = onTap)
            Spacer(Modifier.height(10.dp))
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
            Text(hint, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
            if (done) {
                Text(L.done, style = MaterialTheme.typography.labelSmall, color = cs.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DuaTile(minutes: Int, goal: Int, modifier: Modifier, onTap: () -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val wc = LocalWorshipColors.current
    val reached = goal > 0 && minutes >= goal
    Card(
        modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (minutes > 0) cs.secondaryContainer.copy(alpha = 0.6f) else cs.surfaceContainer),
        border = BorderStroke(1.dp, if (minutes > 0) wc.gold.copy(alpha = 0.6f) else cs.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).clickable(onClick = onTap).padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WorshipCircle(
                filled = reached,
                size = 70.dp,
                label = L.n(minutes),
                labelSize = 22.sp,
                color = wc.gold,
                progress = if (goal > 0) minutes.toFloat() / goal else 0f,
                onTap = onTap,
            )
            Spacer(Modifier.height(10.dp))
            Text(L.dua, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                L.digits("${L.minShort} · ${L.goalOf(goal.toString())}"),
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
