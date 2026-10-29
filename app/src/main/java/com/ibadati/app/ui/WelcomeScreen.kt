package com.ibadati.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.ibadati.app.R

/** First launch: choose English or Arabic. Shown in both languages at once. */
@Composable
fun WelcomeScreen(onChoose: (arabic: Boolean) -> Unit) {
    val gold = Color(0xFFE6C27A)
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) { fade.animateTo(1f, tween(900)) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F5E4E), Color(0xFF082F27)))),
    ) {
        GeometricPattern(Modifier.fillMaxSize(), color = gold, alpha = 0.09f, cell = 56.dp)
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(28.dp)
                .alpha(fade.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(112.dp).clip(RoundedCornerShape(30.dp)).background(Color(0xFF0F4F42)),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(150.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("عبادتي", fontFamily = Amiri, fontSize = 52.sp, color = gold, fontWeight = FontWeight.Bold)
            Text("Ibadati", fontFamily = Plex, fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Text(Lang(true, false).welcomeTagAr, fontFamily = Plex, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
            Text(Lang(false, false).welcomeTagEn, fontFamily = Plex, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, fontSize = 13.sp)
            Spacer(Modifier.height(48.dp))
            Text(
                "${Lang(false, false).chooseLanguageEn}  ·  ${Lang(true, false).chooseLanguageAr}",
                fontFamily = Plex,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(14.dp))
            LanguageButton("العربية", gold, Color(0xFF2B1E04)) { onChoose(true) }
            Spacer(Modifier.height(12.dp))
            LanguageButton("English", Color.White.copy(alpha = 0.12f), Color.White) { onChoose(false) }
        }
    }
}

@Composable
private fun LanguageButton(text: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = fg, fontFamily = Plex, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
