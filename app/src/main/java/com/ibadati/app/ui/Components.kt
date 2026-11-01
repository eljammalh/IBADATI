package com.ibadati.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// ---------------------------------------------------------------- Cards

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            content()
        }
    }
}

/** Emerald hero card with a soft Islamic star pattern behind its content. */
@Composable
fun HeroCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val dark = LocalWorshipColors.current.dark
    val top = if (dark) Color(0xFF15503F) else Color(0xFF0F5E4E)
    val bottom = if (dark) Color(0xFF0B3229) else Color(0xFF0A4339)
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(top, bottom))),
    ) {
        GeometricPattern(Modifier.matchParentSize(), color = Color(0xFFE6C27A), alpha = 0.10f)
        Box(Modifier.padding(20.dp), content = content)
    }
}

/** Tiled eight-pointed stars (khatam), drawn lightly as a background accent. */
@Composable
fun GeometricPattern(modifier: Modifier = Modifier, color: Color, alpha: Float = 0.08f, cell: Dp = 46.dp) {
    Canvas(modifier) {
        val c = cell.toPx()
        val stroke = Stroke(width = 1.1.dp.toPx())
        clipRect {
            var y = -c / 2
            var row = 0
            while (y < size.height + c) {
                var x = if (row % 2 == 0) -c / 2 else 0f
                while (x < size.width + c) {
                    drawStar(Offset(x, y), c * 0.34f, color.copy(alpha = alpha), stroke)
                    x += c
                }
                y += c * 0.87f
                row++
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, r: Float, color: Color, stroke: Stroke) {
    val path = Path()
    val inner = r * 0.62f
    for (i in 0 until 16) {
        val rad = if (i % 2 == 0) r else inner
        val a = (i * PI / 8 - PI / 2).toFloat()
        val p = Offset(center.x + rad * cos(a), center.y + rad * sin(a))
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    drawPath(path, color, style = stroke)
    drawCircle(color, radius = inner * 0.45f, center = center, style = stroke)
}

// ---------------------------------------------------------------- Circles

/**
 * A tappable circle that fills with a springy animation.
 * When [progress] is given (e.g. dua minutes), it draws a progress ring instead of a full fill.
 */
@Composable
fun WorshipCircle(
    filled: Boolean,
    size: Dp,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    labelSize: TextUnit = (size.value * 0.34f).sp,
    progress: Float? = null,
    onLongPress: (() -> Unit)? = null,
    onTap: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val fill by animateFloatAsState(
        targetValue = if (filled) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "fill",
    )
    val press by animateFloatAsState(if (pressed) 0.9f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "press")
    val prog by animateFloatAsState(progress ?: 0f, tween(600), label = "progress")
    val track = MaterialTheme.colorScheme.outlineVariant
    val surface = MaterialTheme.colorScheme.surfaceContainerLowest
    val textColor = lerp(MaterialTheme.colorScheme.onSurface, Color.White, fill.coerceIn(0f, 1f))

    Box(
        modifier
            .size(size)
            .scale(press)
            .pointerInput(onLongPress != null) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTap()
                    },
                    onLongPress = if (onLongPress != null) {
                        { _: Offset ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress?.invoke()
                        }
                    } else null,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = min(this.size.width, this.size.height) / 2
            val sw = (r * 0.09f).coerceAtLeast(2.dp.toPx())
            drawCircle(surface, radius = r - sw / 2)
            drawCircle(track, radius = r - sw / 2, style = Stroke(sw))
            if (progress != null) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * prog.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(sw / 2, sw / 2),
                    size = Size(this.size.width - sw, this.size.height - sw),
                    style = Stroke(sw * 1.4f, cap = StrokeCap.Round),
                )
            }
            if (fill > 0.001f) {
                val f = fill.coerceAtLeast(0f)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(lerp(color, Color.White, 0.18f), color),
                        center = Offset(this.size.width * 0.4f, this.size.height * 0.35f),
                        radius = r * 1.2f,
                    ),
                    radius = (r - sw * 0.2f) * f.coerceAtMost(1.08f),
                )
            }
        }
        Text(
            label,
            color = if (progress != null && !filled) MaterialTheme.colorScheme.onSurface else textColor,
            fontSize = labelSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/** A circle with a small caption below it. */
@Composable
fun CaptionedCircle(caption: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        content()
        Spacer(Modifier.height(4.dp))
        Text(
            caption,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/** A large progress ring with content in the middle. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.outlineVariant,
    stroke: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "ring")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            val tl = Offset(sw / 2, sw / 2)
            val sz = Size(size.width - sw, size.height - sw)
            drawArc(track, -90f, 360f, false, tl, sz, style = Stroke(sw))
            drawArc(color, -90f, 360f * p, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round))
        }
        content()
    }
}

// ---------------------------------------------------------------- Small pieces

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier, accent: Color = MaterialTheme.colorScheme.primary) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent, maxLines = 1)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
fun Bar(fraction: Double, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary, height: Dp = 8.dp) {
    val f by animateFloatAsState(fraction.toFloat().coerceIn(0f, 1f), tween(700), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(f)
                .height(height)
                .clip(RoundedCornerShape(height))
                .background(color),
        )
    }
}

@Composable
fun LabeledBar(label: String, value: String, fraction: Double, color: Color = MaterialTheme.colorScheme.primary) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
        }
        Bar(fraction, color = color)
    }
}

/** Selectable pill used for quick choices. */
@Composable
fun Pill(text: String, selected: Boolean, modifier: Modifier = Modifier, compact: Boolean = false, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = if (compact) 4.dp else 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = fg,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------- Date navigator

@Composable
fun DateNavigator(
    selected: Long,
    today: Long,
    showHijri: Boolean,
    hasData: (Long) -> Boolean,
    onSelect: (Long) -> Unit,
) {
    val L = LocalLang.current
    var showCalendar by remember { mutableStateOf(false) }
    val date = LocalDate.ofEpochDay(selected)
    val title = when (selected) {
        today -> L.today
        today - 1 -> L.yesterday
        else -> L.weekday(date.dayOfWeek)
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onSelect(selected - 1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showCalendar = true }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val sub = if (selected == today || selected == today - 1) "${L.weekday(date.dayOfWeek)} · ${L.date(selected)}" else L.date(selected)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (showHijri) {
                    L.hijri(selected)?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = LocalWorshipColors.current.gold, fontWeight = FontWeight.Medium)
                    }
                }
            }
            IconButton(onClick = { onSelect(selected + 1) }, enabled = selected < today) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
        if (selected != today) {
            TextButton(onClick = { onSelect(today) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(L.backToToday)
            }
        }
    }
    if (showCalendar) {
        CalendarDialog(selected, today, hasData, onDismiss = { showCalendar = false }) {
            showCalendar = false
            onSelect(it)
        }
    }
}

// ---------------------------------------------------------------- Celebration

/** Gold sparks bursting outward, looping softly. */
@Composable
fun Sparkles(modifier: Modifier = Modifier, color: Color = LocalWorshipColors.current.gold) {
    val t = rememberInfiniteTransition(label = "sparkles")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "p")
    val intro = remember { Animatable(0f) }
    LaunchedEffect(Unit) { intro.animateTo(1f, tween(700)) }
    Canvas(modifier) {
        val c = Offset(size.width / 2, size.height / 2)
        val maxR = min(size.width, size.height) / 2
        for (i in 0 until 24) {
            val a = (i * 2 * PI / 24).toFloat()
            val phase = (p + (i % 3) / 3f) % 1f
            val d = maxR * (0.25f + 0.75f * phase) * intro.value
            val pos = Offset(c.x + d * cos(a), c.y + d * sin(a))
            drawStar(pos, 5.dp.toPx() * (1f - phase * 0.5f), color.copy(alpha = (1f - phase) * 0.9f), Stroke(1.6.dp.toPx()))
        }
    }
}
