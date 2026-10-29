package com.ibadati.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState

@Composable
fun DhikrScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    ScreenColumn {
        DateNavigator(state.selected, state.today, state.settings.showHijri, state.records::hasAnyData, vm::selectDay)
        CounterCard(
            title = L.istighfar,
            phrase = L.istighfarPhrase,
            count = state.day.istighfar,
            goal = state.settings.istighfarGoal,
            color = MaterialTheme.colorScheme.primary,
            onSet = vm::setIstighfar,
            onAdd = vm::addIstighfar,
        )
        CounterCard(
            title = L.salawat,
            phrase = L.salawatPhrase,
            count = state.day.salawat,
            goal = state.settings.salawatGoal,
            color = wc.gold,
            onSet = vm::setSalawat,
            onAdd = vm::addSalawat,
        )
    }
}

@Composable
private fun CounterCard(
    title: String,
    phrase: String,
    count: Int,
    goal: Int,
    color: Color,
    onSet: (Int) -> Unit,
    onAdd: (Int) -> Unit,
) {
    val L = LocalLang.current
    val focus = LocalFocusManager.current
    var text by remember { mutableStateOf(if (count == 0) "" else count.toString()) }
    // Keep the box in sync when the count changes from buttons or another day is selected.
    LaunchedEffect(count) {
        if ((parseNumber(text) ?: 0) != count) text = if (count == 0) "" else count.toString()
    }

    SectionCard {
        Text(phrase, fontFamily = Amiri, fontSize = 22.sp, color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = if (goal > 0) count.toFloat() / goal else 0f,
                modifier = Modifier.size(110.dp),
                color = color,
                stroke = 10.dp,
            ) {
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        (slideInVertically(tween(250)) { it / 2 } + fadeIn(tween(250)))
                            .togetherWith(slideOutVertically(tween(250)) { -it / 2 } + fadeOut(tween(200)))
                    },
                    label = "count",
                ) { c ->
                    Text(L.n(c), fontSize = if (c >= 10000) 20.sp else 28.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (goal > 0) {
                    Text(
                        L.digits("${L.pct(count.toDouble() / goal)} ${L.ofGoal} (${goal})"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (count > 0) {
                    TextButton(onClick = { onSet(0) }) { Text(L.reset) }
                }
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { t ->
                val clean = t.filter { it.isDigit() }.take(7)
                text = clean
                onSet(parseNumber(clean) ?: 0)
            },
            label = { Text(L.typeCount) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(10, 33, 100).forEach { v ->
                Pill(L.digits("+$v"), selected = false, modifier = Modifier.weight(1f)) {
                    focus.clearFocus()
                    onAdd(v)
                }
            }
        }
    }
}
