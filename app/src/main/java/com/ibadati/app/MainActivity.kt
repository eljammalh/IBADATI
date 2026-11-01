package com.ibadati.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibadati.app.ui.Amiri
import com.ibadati.app.ui.AthkarScreen
import com.ibadati.app.ui.DhikrScreen
import com.ibadati.app.ui.IbadatiTheme
import com.ibadati.app.ui.Lang
import com.ibadati.app.ui.LocalLang
import com.ibadati.app.ui.LocalWorshipColors
import com.ibadati.app.ui.PrayersScreen
import com.ibadati.app.ui.QuranScreen
import com.ibadati.app.ui.ReportsScreen
import com.ibadati.app.ui.ChallengeScreen
import com.ibadati.app.ui.FastingScreen
import com.ibadati.app.ui.HifzScreen
import com.ibadati.app.ui.MoreScreen
import com.ibadati.app.ui.ReadingScreen
import com.ibadati.app.ui.Section
import com.ibadati.app.ui.SinsScreen
import com.ibadati.app.ui.sectionTitle
import com.ibadati.app.ui.tabMore
import com.ibadati.app.ui.back
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.lifecycle.ViewModelProvider
import com.ibadati.app.ui.SettingsScreen
import com.ibadati.app.ui.WelcomeScreen
import com.ibadati.app.ui.appName
import com.ibadati.app.ui.isDark
import com.ibadati.app.ui.settings
import com.ibadati.app.ui.tabAthkar
import com.ibadati.app.ui.tabDhikr
import com.ibadati.app.ui.tabPrayers
import com.ibadati.app.ui.tabQuran
import com.ibadati.app.ui.tabReports

class MainActivity : ComponentActivity() {
    /** Lock the private section whenever the app leaves the screen. */
    override fun onStop() {
        super.onStop()
        ViewModelProvider(this)[MainViewModel::class.java].lockSins()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val s = state.settings
            val dark = isDark(s.theme)
            val lightBars = !dark && state.loaded && s.onboarded
            DisposableEffect(lightBars) {
                enableEdgeToEdge(
                    statusBarStyle = if (lightBars) SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    else SystemBarStyle.dark(AndroidColor.TRANSPARENT),
                    navigationBarStyle = if (lightBars) SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    else SystemBarStyle.dark(AndroidColor.TRANSPARENT),
                )
                onDispose {}
            }
            IbadatiTheme(s.theme) {
                CompositionLocalProvider(
                    LocalLang provides Lang(s.arabic, s.arabicDigits),
                    LocalLayoutDirection provides if (s.arabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
                ) {
                    when {
                        !state.loaded -> Box(
                            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator() }
                        !s.onboarded -> WelcomeScreen { vm.chooseLanguage(it) }
                        else -> AppRoot(state, vm)
                    }
                }
            }
        }
    }
}

private data class Tab(val label: String, val icon: Int)

@Composable
private fun AppRoot(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var section by rememberSaveable { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val sinsUnlocked by vm.sinsUnlocked.collectAsStateWithLifecycle()
    val tabs = listOf(
        Tab(L.tabPrayers, R.drawable.ic_tab_prayer),
        Tab(L.tabAthkar, R.drawable.ic_tab_athkar),
        Tab(L.tabQuran, R.drawable.ic_tab_quran),
        Tab(L.tabDhikr, R.drawable.ic_tab_dhikr),
        Tab(L.tabMore, R.drawable.ic_tab_more),
    )
    val openSection = if (tab == MORE) section else null

    // The private section locks again as soon as it is left.
    LaunchedEffect(openSection) {
        if (openSection != Section.SINS) vm.lockSins()
    }
    BackHandler(enabled = !showSettings && openSection != null) { section = null }
    BackHandler(enabled = !showSettings && openSection == null && tab != 0) { tab = 0 }

    AnimatedContent(
        targetState = showSettings,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300))) togetherWith fadeOut(tween(200))
            } else {
                fadeIn(tween(300)) togetherWith (slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(250)))
            }
        },
        label = "settings",
    ) { settingsOpen ->
        if (settingsOpen) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
                SettingsScreen(state, vm, onBack = { showSettings = false })
            }
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = if (openSection != null) 4.dp else 20.dp, end = 8.dp, top = 6.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (openSection != null) {
                            IconButton(onClick = { section = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back)
                            }
                            Text(
                                sectionTitle(openSection),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        } else {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    L.appName,
                                    fontFamily = Amiri,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Text(
                                tabs[tab].label,
                                style = MaterialTheme.typography.titleSmall,
                                color = LocalWorshipColors.current.gold,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = L.settings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                        tabs.forEachIndexed { i, t ->
                            NavigationBarItem(
                                selected = tab == i,
                                onClick = {
                                    if (i == MORE && tab == MORE) section = null
                                    tab = i
                                },
                                icon = { Icon(painterResource(t.icon), contentDescription = null) },
                                label = { Text(t.label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                },
            ) { padding ->
                val page = if (tab == MORE) "more:" + (section ?: "") else "tab:$tab"
                AnimatedContent(
                    targetState = page,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                    modifier = Modifier.padding(padding),
                    label = "tabs",
                ) { p ->
                    when (p) {
                        "tab:0" -> PrayersScreen(state, vm)
                        "tab:1" -> AthkarScreen(state, vm)
                        "tab:2" -> QuranScreen(state, vm)
                        "tab:3" -> DhikrScreen(state, vm)
                        "more:" + Section.CHALLENGE -> ChallengeScreen(state, vm)
                        "more:" + Section.FASTING -> FastingScreen(state, vm)
                        "more:" + Section.HIFZ -> HifzScreen(state, vm)
                        "more:" + Section.READING -> ReadingScreen(state, vm)
                        "more:" + Section.REPORTS -> ReportsScreen(state)
                        "more:" + Section.SINS -> SinsScreen(state, vm, sinsUnlocked)
                        else -> MoreScreen(state) { section = it }
                    }
                }
            }
        }
    }
}

private const val MORE = 4
