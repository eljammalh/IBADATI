package com.ibadati.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.logic.Challenge
import com.ibadati.app.logic.Stats
import kotlinx.coroutines.delay

private const val HADITH =
    "«مَنْ صَلَّى لِلَّهِ أَرْبَعِينَ يَوْمًا فِي جَمَاعَةٍ، يُدْرِكُ التَّكْبِيرَةَ الأُولَى، كُتِبَتْ لَهُ بَرَاءَتَانِ: بَرَاءَةٌ مِنَ النَّارِ، وَبَرَاءَةٌ مِنَ النِّفَاقِ»"

@Composable
fun ChallengeScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val ch = remember(state.records, state.today) { Stats.challenge(state.records, state.today) }
    var celebrate by remember { mutableStateOf(false) }

    // Celebrate each newly completed challenge once.
    LaunchedEffect(ch.completions.size) {
        if (ch.completions.size > state.settings.challengesSeen) {
            celebrate = true
            vm.markChallengesSeen(ch.completions.size)
        }
    }

    ScreenColumn {
        HeroCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    HADITH,
                    fontFamily = Amiri,
                    fontSize = 21.sp,
                    lineHeight = 38.sp,
                    color = Color(0xFFF3E4BE),
                    textAlign = TextAlign.Center,
                )
                Text("رواه الترمذي", fontFamily = Amiri, fontSize = 14.sp, color = Color(0xFFE6C27A))
                if (!L.ar) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        L.hadithTranslation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = ch.current / 40f,
                    modifier = Modifier.size(96.dp),
                    color = wc.mosque,
                    stroke = 10.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(L.n(ch.current), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = wc.mosque)
                        Text(L.digits("/40"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(L.dayOfForty(ch.current), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${L.pct(ch.current / 40.0)} · ${L.daysLeft(Challenge.LENGTH - ch.current)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ch.startDay?.let {
                        Text(L.startedOn(L.shortDate(it)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        if (ch.todayDone) L.todayCounts else L.todayPending,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (ch.todayDone) wc.mosque else wc.gold,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (ch.current == 0 && !ch.todayDone) {
                Text(L.notStarted, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SectionCard {
            FortyGrid(ch)
            Text(L.challengeRule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(L.daysCount(ch.best), L.highestStreak, Modifier.weight(1f), accent = wc.mosque)
            StatTile(L.n(ch.completions.size), L.challengesCompleted, Modifier.weight(1f), accent = wc.gold)
        }

        if (ch.completions.isNotEmpty()) {
            SectionCard(title = L.challengesCompleted) {
                ch.completions.reversed().forEachIndexed { i, d ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(wc.gold.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                            Text(L.n(ch.completions.size - i), color = wc.gold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("${L.completedOn} · ${L.date(d)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (celebrate) {
        Dialog(onDismissRequest = { celebrate = false }) {
            Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                HeroCard {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Sparkles(Modifier.fillMaxWidth().height(260.dp))
                        Column(
                            Modifier.padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(L.digits("40"), fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE6C27A))
                            Text(L.challengeDoneTitle, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(L.challengeDoneText, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFE6C27A))
                                    .clickable { celebrate = false }
                                    .padding(horizontal = 28.dp, vertical = 12.dp),
                            ) {
                                Text(L.ameen, color = Color(0xFF2B1E04), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 5 rows × 8 cells; completed days fill one after another with a little pop. */
@Composable
private fun FortyGrid(ch: Challenge) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until 5) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (col in 0 until 8) {
                    val i = row * 8 + col
                    val done = i < ch.current
                    val next = i == ch.current
                    var shown by remember { mutableStateOf(false) }
                    LaunchedEffect(done) {
                        if (done) delay(18L * i)
                        shown = done
                    }
                    val s by animateFloatAsState(if (shown) 1f else 0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow), label = "cell")
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cs.surfaceContainerHigh)
                            .border(
                                if (next) 1.5.dp else 1.dp,
                                if (next) wc.gold else cs.outlineVariant.copy(alpha = 0.6f),
                                RoundedCornerShape(10.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .scale(s)
                                .clip(RoundedCornerShape(10.dp))
                                .background(wc.mosque),
                        )
                        Text(
                            L.n(i + 1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (shown) Color.White else cs.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
