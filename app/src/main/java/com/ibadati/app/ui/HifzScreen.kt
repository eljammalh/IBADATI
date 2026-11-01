package com.ibadati.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ibadati.app.data.Mushaf
import com.ibadati.app.data.Surah
import com.ibadati.app.logic.Memorization
import com.ibadati.app.logic.Memorize

@Composable
fun HifzScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val memorized = state.records.memorized
    val m = remember(memorized) { Memorize.compute(memorized) }
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val list = Mushaf.surahs.filter {
        q.isEmpty() || it.nameEn.contains(q, ignoreCase = true) || it.nameAr.contains(q) ||
            it.id.toString() == q || parseNumber(q) == it.id
    }

    ScreenColumn {
        HeroCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = m.fraction.toFloat(),
                        modifier = Modifier.size(104.dp),
                        color = Color(0xFFE6C27A),
                        track = Color.White.copy(alpha = 0.15f),
                        stroke = 10.dp,
                    ) {
                        Text(L.pct(m.fraction), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(L.ofTheQuran, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroStat(L.ofN(m.surahs, 114), L.surahsMemorized, Modifier.weight(1f))
                    HeroStat(L.digits("${L.dec(m.pages)}/604"), L.pagesMemorized, Modifier.weight(1f))
                    HeroStat(L.ofN(m.fullJuz, 30), L.juzMemorized, Modifier.weight(1f))
                }
            }
        }

        SectionCard(title = L.juzGrid) {
            JuzGrid(m)
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            placeholder = { Text(L.searchSurah) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(L.tapSurahHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        list.forEach { s ->
            SurahRow(s, s.id in memorized) { vm.toggleMemorized(s.id) }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Color(0xFFE6C27A), fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun JuzGrid(m: Memorization) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val wc = LocalWorshipColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until 5) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (col in 0 until 6) {
                    val j = row * 6 + col + 1
                    val f = (m.perJuz[j - 1] / Memorization.juzLength(j)).coerceIn(0.0, 1.0).toFloat()
                    val full = f >= 0.995f
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cs.surfaceContainerHigh)
                            .border(1.dp, if (full) wc.gold else cs.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        // Fills from the bottom like a glass.
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(f)
                                .background(if (full) wc.gold else cs.primary.copy(alpha = 0.75f)),
                        )
                        Text(
                            L.n(j),
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (f > 0.5f) Color.White else cs.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahRow(s: Surah, memorized: Boolean, onClick: () -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val wc = LocalWorshipColors.current
    val bg by animateColorAsState(if (memorized) cs.primaryContainer.copy(alpha = 0.55f) else cs.surfaceContainer, label = "surahBg")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, if (memorized) cs.primary.copy(alpha = 0.45f) else cs.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(if (memorized) cs.primary else wc.gold.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            if (memorized) Icon(Icons.Filled.Check, null, tint = cs.onPrimary, modifier = Modifier.size(20.dp))
            else Text(L.n(s.id), fontWeight = FontWeight.Bold, color = wc.gold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(s.nameAr, fontFamily = Amiri, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                (if (L.ar) "" else "${s.nameEn} · ") + "${L.ayahs(s.ayahs)} · ${L.pages(maxOf(1, Math.round(s.length).toInt()))}",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Text(
            L.juzN(s.juzStart),
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}
