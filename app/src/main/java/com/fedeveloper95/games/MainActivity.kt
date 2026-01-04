package com.fedeveloper95.games

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.Calendar
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlex = FontFamily(
    Font(
        resId = R.font.sans_flex,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexThin = FontFamily(
    Font(
        resId = R.font.sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.slant(-9f),
            FontVariation.width(111f),
            FontVariation.weight(333),
            FontVariation.Setting("GRAD", 100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

val ExpressiveTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun GameHubTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    val savedTheme = remember { mutableIntStateOf(prefs.getInt("pref_theme", 0)) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "pref_theme") {
                savedTheme.intValue = sharedPreferences.getInt("pref_theme", 0)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val darkTheme = when (savedTheme.intValue) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD0BCFF),
                onPrimary = Color(0xFF381E72),
                primaryContainer = Color(0xFF4F378B),
                onPrimaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFFCCC2DC),
                background = Color(0xFF141218),
                surface = Color(0xFF141218),
                surfaceContainer = Color(0xFF211F26)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF6750A4),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEADDFF),
                onPrimaryContainer = Color(0xFF21005D),
                secondary = Color(0xFF625B71),
                background = Color(0xFFF9F9FF),
                surface = Color(0xFFF9F9FF),
                surfaceContainer = Color(0xFFE7E0EC)
            )
        }
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        content = content
    )
}

data class GameApp(
    val name: String,
    val packageName: String,
    val launchIntent: Intent?,
    val launchCount: Int = 0,
    val totalPlayTime: Long = 0
)

class GameViewModel : ViewModel() {
    private val _games = MutableStateFlow<List<GameApp>>(emptyList())
    val games: StateFlow<List<GameApp>> = _games

    private val _allApps = MutableStateFlow<List<GameApp>>(emptyList())
    val allApps: StateFlow<List<GameApp>> = _allApps

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val HIDDEN_GAMES_PREF = "hidden_games"
    private val MANUAL_GAMES_PREF = "manual_games"
    private val GAME_ORDER_PREF = "game_order"
    private val PREF_PREFIX_COUNT = "play_count_"
    private val PREF_SORT_TYPE = "pref_sort_type"
    private val PREF_STATS_INTERVAL = "pref_stats_interval"

    private var loadJob: Job? = null

    fun loadGames(context: Context) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            val packageManager = context.packageManager
            val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
            val settings = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)

            val loadedData = withContext(Dispatchers.IO) {
                try {
                    val hiddenGames = getPrefsSet(context, HIDDEN_GAMES_PREF)
                    val manualGames = getPrefsSet(context, MANUAL_GAMES_PREF)
                    val savedOrder = getSavedOrder(context)
                    val sortType = settings.getString(PREF_SORT_TYPE, "Alphabetical") ?: "Alphabetical"
                    val statsInterval = settings.getFloat(PREF_STATS_INTERVAL, 3f).roundToInt()

                    val intent = Intent(Intent.ACTION_MAIN, null)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    val resolveInfos = packageManager.queryIntentActivities(intent, 0)

                    val gamesList = mutableListOf<GameApp>()
                    val allAppsList = mutableListOf<GameApp>()

                    val usageStats = getUsageStats(context, statsInterval)

                    for (resolveInfo in resolveInfos) {
                        val packageName = resolveInfo.activityInfo.packageName ?: continue
                        val name = resolveInfo.loadLabel(packageManager).toString()
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: continue

                        val appInfo = try {
                            packageManager.getApplicationInfo(packageName, 0)
                        } catch (e: PackageManager.NameNotFoundException) { continue }

                        val isDeclaredGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appInfo.category == ApplicationInfo.CATEGORY_GAME
                        } else {
                            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                        }

                        val isManualGame = manualGames.contains(packageName)
                        val isHidden = hiddenGames.contains(packageName)
                        val count = prefs.getInt(PREF_PREFIX_COUNT + packageName, 0)
                        val time = usageStats[packageName] ?: 0L

                        val app = GameApp(name, packageName, launchIntent, count, time)

                        if (!isDeclaredGame || isHidden) {
                            if (!isManualGame) allAppsList.add(app)
                        }

                        if ((isDeclaredGame || isManualGame) && !isHidden) {
                            gamesList.add(app)
                        }
                    }

                    val sortedGames = when (sortType) {
                        "Time" -> gamesList.sortedByDescending { it.totalPlayTime }
                        "Custom" -> {
                            if (savedOrder.isNotEmpty()) {
                                gamesList.sortedBy { game ->
                                    val index = savedOrder.indexOf(game.packageName)
                                    if (index != -1) index else Int.MAX_VALUE
                                }
                            } else {
                                gamesList.sortedBy { it.name }
                            }
                        }
                        else -> gamesList.sortedBy { it.name }
                    }

                    Pair(sortedGames, allAppsList.sortedBy { it.name })
                } catch (e: Exception) {
                    Pair(emptyList(), emptyList())
                }
            }

            _games.value = loadedData.first
            _allApps.value = loadedData.second
            _isLoading.value = false
        }
    }

    private fun getUsageStats(context: Context, intervalIndex: Int): Map<String, Long> {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }

        if (mode != AppOpsManager.MODE_ALLOWED) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        when (intervalIndex) {
            0 -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            1 -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            2 -> calendar.add(Calendar.MONTH, -1)
            3 -> calendar.add(Calendar.YEAR, -1)
        }
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val usageMap = mutableMapOf<String, Long>()

        if (stats != null) {
            for (usageStat in stats) {
                val current = usageMap[usageStat.packageName] ?: 0L
                usageMap[usageStat.packageName] = current + usageStat.totalTimeInForeground
            }
        }
        return usageMap
    }

    fun incrementLaunchCount(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(PREF_PREFIX_COUNT + packageName, 0)
        prefs.edit().putInt(PREF_PREFIX_COUNT + packageName, currentCount + 1).apply()

        val updatedGames = _games.value.map { game ->
            if (game.packageName == packageName) {
                game.copy(launchCount = currentCount + 1)
            } else {
                game
            }
        }
        _games.value = updatedGames
    }

    fun updateGamesOrder(newOrder: List<GameApp>) {
        _games.value = newOrder
    }

    fun saveOrder(context: Context) {
        val orderList = _games.value.map { it.packageName }
        val orderString = orderList.joinToString(",")
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(GAME_ORDER_PREF, orderString).apply()
    }

    private fun getSavedOrder(context: Context): List<String> {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val orderString = prefs.getString(GAME_ORDER_PREF, "") ?: ""
        return if (orderString.isNotEmpty()) orderString.split(",") else emptyList()
    }

    fun hideGame(context: Context, packageName: String) {
        removeFromPrefs(context, MANUAL_GAMES_PREF, packageName)
        addToPrefs(context, HIDDEN_GAMES_PREF, packageName)
        loadGames(context)
    }

    fun addManualGame(context: Context, packageName: String) {
        removeFromPrefs(context, HIDDEN_GAMES_PREF, packageName)
        addToPrefs(context, MANUAL_GAMES_PREF, packageName)
        loadGames(context)
    }

    private fun getPrefsSet(context: Context, key: String): Set<String> {
        val prefs: SharedPreferences = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    private fun addToPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(value)
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun removeFromPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (set.contains(value)) {
            set.remove(value)
            prefs.edit().putStringSet(key, set).apply()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GameHubScreen()
                }
            }
        }
    }
}

enum class ViewType { Pager, Grid, List }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalTextApi::class
)
@Composable
fun GameHubScreen(viewModel: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var gameToRemove by remember { mutableStateOf<GameApp?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val currentCardStyle = remember { mutableStateOf(prefs.getString("pref_card_style", "Default") ?: "Default") }
    val showGetMoreGames = remember { mutableStateOf(prefs.getBoolean("pref_show_get_more_games", true)) }
    val autoUpdates = remember { mutableStateOf(prefs.getBoolean("pref_auto_updates", true)) }

    val showUserName = remember { mutableStateOf(prefs.getBoolean("pref_show_user_name", true)) }
    val userName = remember { mutableStateOf(prefs.getString("pref_user_name", "User") ?: "User") }
    val sortType = remember { mutableStateOf(prefs.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical") }
    val statsInterval = remember { mutableFloatStateOf(prefs.getFloat("pref_stats_interval", 3f)) }


    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (autoUpdates.value) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val update = Updater.checkForUpdates(currentVersionName)
            if (update != null) {
                Updater.showUpdateNotification(context, update)
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "pref_card_style") {
                currentCardStyle.value = sharedPreferences.getString("pref_card_style", "Default") ?: "Default"
            }
            if (key == "pref_show_get_more_games") {
                showGetMoreGames.value = sharedPreferences.getBoolean("pref_show_get_more_games", true)
            }
            if (key == "pref_auto_updates") {
                autoUpdates.value = sharedPreferences.getBoolean("pref_auto_updates", true)
            }
            if (key == "pref_show_user_name") {
                showUserName.value = sharedPreferences.getBoolean("pref_show_user_name", true)
            }
            if (key == "pref_user_name") {
                userName.value = sharedPreferences.getString("pref_user_name", "User") ?: "User"
            }
            if (key == "pref_sort_type") {
                sortType.value = sharedPreferences.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical"
                viewModel.loadGames(context)
            }
            if (key == "pref_stats_interval") {
                statsInterval.floatValue = sharedPreferences.getFloat("pref_stats_interval", 3f)
                viewModel.loadGames(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(LocalLifecycleOwner.current.lifecycle) {
        viewModel.loadGames(context)
    }

    val displayGames = remember(games, searchQuery) {
        if (searchQuery.isEmpty()) games
        else games.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val openPlayStore: (String) -> Unit = { packageName ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    val launchGame: (GameApp) -> Unit = { game ->
        game.launchIntent?.let {
            viewModel.incrementLaunchCount(context, game.packageName)
            context.startActivity(it)
        }
    }

    val headerItemCount = 2
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            if (searchQuery.isNotEmpty()) return@rememberReorderableLazyListState
            if (from.index < headerItemCount || to.index < headerItemCount) return@rememberReorderableLazyListState
            if (sortType.value != "Custom") return@rememberReorderableLazyListState

            val currentList = games.toMutableList()
            val fromIndex = from.index - headerItemCount
            val toIndex = to.index - headerItemCount

            if (fromIndex in currentList.indices && toIndex in currentList.indices) {
                val item = currentList.removeAt(fromIndex)
                currentList.add(toIndex, item)
                viewModel.updateGamesOrder(currentList)
            }
        }
    )

    val reorderGridState = rememberReorderableLazyGridState(
        onMove = { from, to ->
            if (searchQuery.isNotEmpty()) return@rememberReorderableLazyGridState
            if (from.index < headerItemCount || to.index < headerItemCount) return@rememberReorderableLazyGridState
            if (sortType.value != "Custom") return@rememberReorderableLazyGridState

            val currentList = games.toMutableList()
            val fromIndex = from.index - headerItemCount
            val toIndex = to.index - headerItemCount

            if (fromIndex in currentList.indices && toIndex in currentList.indices) {
                val item = currentList.removeAt(fromIndex)
                currentList.add(toIndex, item)
                viewModel.updateGamesOrder(currentList)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (games.isEmpty()) viewModel.loadGames(context)
    }

    BackHandler(enabled = isEditMode || searchQuery.isNotEmpty()) {
        if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        } else {
            showSaveDialog = true
        }
    }

    val currentViewType = remember(currentCardStyle.value, isEditMode) {
        when {
            currentCardStyle.value == "Horizontal" && !isEditMode -> ViewType.Pager
            currentCardStyle.value == "Grid" -> ViewType.Grid
            else -> ViewType.List
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val showSmallTitle = remember {
        derivedStateOf {
            if (currentViewType == ViewType.Pager) false
            else if (currentViewType == ViewType.List) {
                reorderState.listState.firstVisibleItemIndex > 0
            } else {
                reorderGridState.gridState.firstVisibleItemIndex > 0
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = showSmallTitle.value || isEditMode,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        if (isEditMode) {
                            Text(
                                text = stringResource(R.string.edit_mode_title),
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontFamily = GoogleSansFlex,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    AnimatedContent(
                        targetState = isEditMode,
                        transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() },
                        label = "actionAnim"
                    ) { editMode ->
                        if (editMode) {
                            IconButton(onClick = {
                                viewModel.saveOrder(context)
                                isEditMode = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                            }
                        } else {
                            IconButton(onClick = {
                                val intent = Intent(context, SettingsActivity::class.java)
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                            }
                        }
                    }
                },
                navigationIcon = {
                    AnimatedVisibility(
                        visible = isEditMode,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.discard))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (showSmallTitle.value || isEditMode) MaterialTheme.colorScheme.background else Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isEditMode && searchQuery.isEmpty(),
                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_game))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column {
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 6.dp,
                                strokeCap = StrokeCap.Round,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    } else if (games.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (!isEditMode) {
                                MainHeaderTitle(
                                    showUserName.value,
                                    userName.value,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                            EmptyState(modifier = Modifier.weight(1f))
                        }
                    } else {
                        AnimatedContent(
                            targetState = currentViewType,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "mainContentAnim"
                        ) { viewType ->
                            when (viewType) {
                                ViewType.Pager -> {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        MainHeaderTitle(
                                            showUserName.value,
                                            userName.value,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                        SearchAndCountHeader(
                                            searchQuery = searchQuery,
                                            onQueryChange = { searchQuery = it },
                                            count = displayGames.size,
                                            showSearch = !isEditMode,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                        HorizontalGamePager(
                                            games = displayGames,
                                            onLaunch = launchGame,
                                            onStoreClick = { openPlayStore(it.packageName) },
                                            onLongPress = { if(sortType.value == "Custom") isEditMode = true },
                                            onDelete = { gameToRemove = it }
                                        )
                                    }
                                }
                                ViewType.Grid -> {
                                    val gridShape = remember { RoundedCornerShape(24.dp) }
                                    LazyVerticalGrid(
                                        state = reorderGridState.gridState,
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .reorderable(reorderGridState)
                                    ) {
                                        item(span = { GridItemSpan(maxLineSpan) }, key = "header_title") {
                                            if (!isEditMode) {
                                                MainHeaderTitle(showUserName.value, userName.value)
                                            } else {
                                                Spacer(Modifier.height(16.dp))
                                            }
                                        }
                                        item(span = { GridItemSpan(maxLineSpan) }, key = "header_search") {
                                            if (!isEditMode) {
                                                SearchAndCountHeader(
                                                    searchQuery = searchQuery,
                                                    onQueryChange = { searchQuery = it },
                                                    count = displayGames.size,
                                                    showSearch = true
                                                )
                                            }
                                        }

                                        items(displayGames, key = { it.packageName }) { game ->
                                            ReorderableItem(reorderGridState, key = game.packageName) { isDragging ->
                                                val elevation = animateDpAsState(if (isDragging) 12.dp else 0.dp)
                                                val scale = animateFloatAsState(if (isDragging) 1.05f else 1f)

                                                Box(
                                                    modifier = Modifier
                                                        .aspectRatio(1f)
                                                        .graphicsLayer {
                                                            scaleX = scale.value
                                                            scaleY = scale.value
                                                            shadowElevation = elevation.value.toPx()
                                                            shape = gridShape
                                                            clip = false
                                                        }
                                                        .detectReorderAfterLongPress(reorderGridState)
                                                ) {
                                                    if (!isEditMode) {
                                                        SwipeableGameContainer(
                                                            item = game,
                                                            onDelete = { gameToRemove = game }
                                                        ) {
                                                            GridGameCard(
                                                                game = game,
                                                                isEditMode = false,
                                                                onLaunch = { launchGame(game) },
                                                                onLongPress = { if (searchQuery.isEmpty() && sortType.value == "Custom") isEditMode = true }
                                                            )
                                                        }
                                                    } else {
                                                        GridGameCard(
                                                            game = game,
                                                            isEditMode = true,
                                                            onLaunch = {},
                                                            onLongPress = {},
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (!isEditMode && searchQuery.isEmpty() && showGetMoreGames.value) {
                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                GetMoreGamesCard(context)
                                            }
                                        }
                                    }
                                }
                                ViewType.List -> {
                                    val listShape = remember { RoundedCornerShape(28.dp) }
                                    LazyColumn(
                                        state = reorderState.listState,
                                        contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .reorderable(reorderState)
                                    ) {
                                        item(key = "header_title") {
                                            if (!isEditMode) {
                                                MainHeaderTitle(showUserName.value, userName.value)
                                            } else {
                                                Spacer(Modifier.height(16.dp))
                                            }
                                        }

                                        stickyHeader(key = "header_search") {
                                            if (!isEditMode) {
                                                Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                                                    SearchAndCountHeader(
                                                        searchQuery = searchQuery,
                                                        onQueryChange = { searchQuery = it },
                                                        count = displayGames.size,
                                                        showSearch = true
                                                    )
                                                }
                                            }
                                        }

                                        itemsIndexed(items = displayGames, key = { _, item -> item.packageName }) { index, game ->
                                            ReorderableItem(reorderState, key = game.packageName) { isDragging ->
                                                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                                                val scale = animateFloatAsState(if (isDragging) 1.05f else 1f)

                                                if (!isEditMode) {
                                                    SwipeableGameContainer(
                                                        item = game,
                                                        onDelete = { gameToRemove = game }
                                                    ) {
                                                        GameListItem(
                                                            game = game,
                                                            isEditMode = false,
                                                            onLaunch = { launchGame(game) },
                                                            onStoreClick = { openPlayStore(game.packageName) },
                                                            onLongPress = { if (searchQuery.isEmpty() && sortType.value == "Custom") isEditMode = true }
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .graphicsLayer {
                                                                scaleX = scale.value
                                                                scaleY = scale.value
                                                                shadowElevation = elevation.value.toPx()
                                                                shape = listShape
                                                                clip = false
                                                            }
                                                            .background(MaterialTheme.colorScheme.background)
                                                            .detectReorderAfterLongPress(reorderState)
                                                    ) {
                                                        GameListItem(
                                                            game = game,
                                                            isEditMode = true,
                                                            onLaunch = {},
                                                            onStoreClick = {},
                                                            onLongPress = {},
                                                            isDragging = isDragging
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (!isEditMode && searchQuery.isEmpty() && showGetMoreGames.value) {
                                            item { GetMoreGamesCard(context) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val cornerPercent by animateIntAsState(
            targetValue = if (isPressed) 15 else 50,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "btnMorph"
        )

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.save_order_title),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.save_order_description),
                    fontFamily = GoogleSansFlex
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveOrder(context)
                        isEditMode = false
                        showSaveDialog = false
                    },
                    shape = RoundedCornerShape(cornerPercent),
                    interactionSource = interactionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.loadGames(context)
                    isEditMode = false
                    showSaveDialog = false
                }) {
                    Text(
                        text = stringResource(R.string.discard),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (gameToRemove != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val cornerPercent by animateIntAsState(
            targetValue = if (isPressed) 15 else 50,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "btnMorph"
        )

        AlertDialog(
            onDismissRequest = { gameToRemove = null },
            icon = { Icon(Icons.Default.Delete, null) },
            title = { Text(stringResource(R.string.remove_game_title)) },
            text = { Text(stringResource(R.string.remove_game_desc, gameToRemove!!.name)) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            confirmButton = {
                Button(
                    onClick = {
                        gameToRemove?.let { viewModel.hideGame(context, it.packageName) }
                        gameToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(cornerPercent),
                    interactionSource = interactionSource
                ) { Text(stringResource(R.string.remove)) }
            },
            dismissButton = {
                TextButton(onClick = { gameToRemove = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showAddSheet) {
        AddGameBottomSheet(
            allApps = viewModel.allApps.collectAsState().value,
            onDismiss = { showAddSheet = false },
            onAdd = { pkg ->
                viewModel.addManualGame(context, pkg)
                showAddSheet = false
            }
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun MainHeaderTitle(showUserName: Boolean, userName: String, modifier: Modifier = Modifier) {
    val customWelcomeFontFamily = FontFamily(
        Font(
            resId = R.font.sans_flex,
            variationSettings = FontVariation.Settings(
                FontVariation.slant(-9f),
                FontVariation.width(111f),
                FontVariation.weight(333),
                FontVariation.Setting("GRAD", 100f),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        if (showUserName) {
            Text(
                text = "$userName's",
                style = TextStyle(
                    fontFamily = customWelcomeFontFamily,
                    fontSize = 36.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 56.sp
        )
    }
}

@Composable
fun SearchAndCountHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    count: Int,
    showSearch: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        if (showSearch) {
            HomeSearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = "$count ${stringResource(R.string.games_count_suffix)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val icon = remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                icon.value = context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
            }
        }
    }

    AsyncImage(
        model = icon.value,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HorizontalGamePager(
    games: List<GameApp>,
    onLaunch: (GameApp) -> Unit,
    onStoreClick: (GameApp) -> Unit,
    onLongPress: () -> Unit,
    onDelete: (GameApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { games.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 24.dp),
        pageSpacing = 16.dp,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val game = games[page]

        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val scale = lerp(1f, 0.85f, pageOffset.absoluteValue.coerceIn(0f, 1f))
        val alpha = lerp(1f, 0.5f, pageOffset.absoluteValue.coerceIn(0f, 1f))

        SwipeableGameContainer(
            item = game,
            onDelete = { onDelete(game) },
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            HorizontalGameCard(
                game = game,
                onLaunch = { onLaunch(game) },
                onStoreClick = { onStoreClick(game) },
                onLongPress = onLongPress
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HorizontalGameCard(
    game: GameApp,
    onLaunch: () -> Unit,
    onStoreClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f)
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppIcon(
                packageName = game.packageName,
                modifier = Modifier
                    .size(140.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = game.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(onClick = onStoreClick) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = "Store",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedPlayButton(onClick = onLaunch)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GridGameCard(
    game: GameApp,
    isEditMode: Boolean,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .combinedClickable(
                onClick = { if(!isEditMode) onLaunch() },
                onLongClick = onLongPress,
                enabled = !isEditMode
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (isEditMode) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppIcon(
                    packageName = game.packageName,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isEditMode) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SwipeableGameContainer(
    item: GameApp,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isRevealed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val revealThreshold = with(density) { 100.dp.toPx() }
    val deleteThreshold = with(density) { 150.dp.toPx() }
    val revealOffset = with(density) { -140.dp.toPx() }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "offset"
    )

    val revealProgress = (offsetX / revealOffset).coerceIn(0f, 1f)

    Box(modifier = modifier) {
        if (offsetX > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
        }

        if (offsetX < 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    modifier = Modifier.padding(end = 24.dp).width(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${item.launchCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = formatPlayTime(item.totalPlayTime),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .draggable(
                    state = rememberDraggableState { delta ->
                        val newOffset = offsetX + delta
                        if (isRevealed) {
                            offsetX = newOffset.coerceAtMost(0f)
                        } else {
                            offsetX = newOffset
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX > deleteThreshold) {
                            onDelete()
                            offsetX = 0f
                            isRevealed = false
                        } else if (offsetX < revealOffset + 50f) {
                            offsetX = revealOffset
                            isRevealed = true
                        } else {
                            offsetX = 0f
                            isRevealed = false
                        }
                    }
                )
        ) {
            content()
        }
    }
}

fun formatPlayTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}


@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text(stringResource(R.string.search_apps)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        singleLine = true
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GameListItem(
    game: GameApp,
    isEditMode: Boolean,
    onLaunch: () -> Unit,
    onStoreClick: () -> Unit,
    onLongPress: () -> Unit = {},
    isDragging: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleFactor by animateFloatAsState(
        targetValue = if (isEditMode || isDragging) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onLaunch,
                onLongClick = onLongPress,
                enabled = !isEditMode
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditMode || isDragging) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 0.dp),
        border = if (isEditMode) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                packageName = game.packageName,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
            }

            if (isEditMode) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                IconButton(onClick = onStoreClick) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = "Store Page",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                AnimatedPlayButton(onClick = onLaunch)
            }
        }
    }
}

@Composable
fun GetMoreGamesCard(context: Context) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnMorph"
    )

    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("market://search?q=games&c=apps") }
            try { context.startActivity(intent) } catch (e: Exception) {}
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(cornerPercent),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shadowElevation = 6.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.download_games),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AnimatedPlayButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnMorph"
    )

    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.play),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameBottomSheet(
    allApps: List<GameApp>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isEmpty()) allApps
        else allApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
        ) {
            Text(
                stringResource(R.string.add_to_library),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(filteredApps) { index, app ->
                    val shape = when {
                        filteredApps.size == 1 -> RoundedCornerShape(28.dp)
                        index == 0 -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                        index == filteredApps.size - 1 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
                        else -> RoundedCornerShape(2.dp)
                    }

                    GroupedAppItem(app = app, shape = shape, onAdd = { onAdd(app.packageName) })
                }
            }
        }
    }
}

@Composable
fun GroupedAppItem(
    app: GameApp,
    shape: Shape,
    onAdd: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        onClick = onAdd,
        shape = shape,
        color = if (isPressed) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().height(72.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                packageName = app.packageName,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.SportsEsports, null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.no_games_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(stringResource(R.string.no_games_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}