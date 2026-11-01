package com.ibadati.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ibadati.app.R
import com.ibadati.app.UiState
import com.ibadati.app.logic.Fasting
import com.ibadati.app.logic.Memorize
import com.ibadati.app.logic.Stats

/** Sections reached from the "More" tab. */
object Section {
    const val CHALLENGE = "challenge"
    const val FASTING = "fasting"
    const val HIFZ = "hifz"
    const val READING = "reading"
    const val REPORTS = "reports"
    const val SINS = "sins"
}

@Composable
fun sectionTitle(key: String): String {
    val L = LocalLang.current
    return when (key) {
        Section.CHALLENGE -> L.secChallenge
        Section.FASTING -> L.secFasting
        Section.HIFZ -> L.secHifz
        Section.READING -> L.secReading
        Section.REPORTS -> L.tabReports
        else -> L.secSins
    }
}

private data class MoreItem(val key: String, val title: String, val hint: String, val badge: String?, val color: Color, val icon: Painter)

@Composable
fun MoreScreen(state: UiState, onOpen: (String) -> Unit) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    val challenge = remember(state.records, state.today) { Stats.challenge(state.records, state.today) }
    val memo = remember(state.records.memorized) { Memorize.compute(state.records.memorized) }
    val nextFast = Fasting.next(if (state.records.day(state.today).fasted) state.today + 1 else state.today)
    val readingNow = state.records.books.count { it.completedDay == null }

    val items = listOf(
        MoreItem(Section.CHALLENGE, L.secChallenge, L.secChallengeHint, L.digits("${challenge.current}/40"), wc.mosque, painterResource(R.drawable.ic_tab_prayer)),
        MoreItem(
            Section.FASTING, L.secFasting, L.secFastingHint,
            if (nextFast == state.today) L.today else L.shortDate(nextFast),
            wc.gold, painterResource(R.drawable.ic_sec_fasting),
        ),
        MoreItem(Section.HIFZ, L.secHifz, L.secHifzHint, L.pct(memo.fraction), cs.primary, painterResource(R.drawable.ic_sec_hifz)),
        MoreItem(Section.READING, L.secReading, L.secReadingHint, if (readingNow > 0) L.n(readingNow) else null, cs.tertiary, painterResource(R.drawable.ic_sec_book)),
        MoreItem(Section.REPORTS, L.tabReports, L.secReportsHint, null, Color(0xFF3F7FBF), painterResource(R.drawable.ic_tab_reports)),
        MoreItem(Section.SINS, L.secSins, L.secSinsHint, null, Color(0xFF7A5C8E), rememberVectorPainter(Icons.Filled.Lock)),
    )

    ScreenColumn {
        items.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { item -> MoreCard(item, Modifier.weight(1f)) { onOpen(item.key) } }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MoreCard(item: MoreItem, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier.heightIn(min = 150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        border = BorderStroke(1.dp, item.color.copy(alpha = 0.35f)),
    ) {
        Box(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
            GeometricPattern(Modifier.matchParentSize(), color = item.color, alpha = 0.07f, cell = 40.dp)
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(item.color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(item.icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    if (item.badge != null) {
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).background(item.color.copy(alpha = 0.14f)).padding(horizontal = 9.dp, vertical = 3.dp),
                        ) {
                            Text(item.badge, color = item.color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(item.hint, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2)
            }
        }
    }
}
