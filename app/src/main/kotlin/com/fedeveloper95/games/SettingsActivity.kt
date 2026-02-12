package com.fedeveloper95.games

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(onBack = { finish() })
                }
            }
        }
    }
}

const val PREF_THEME = "pref_theme"
const val THEME_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

const val PREF_CARD_STYLE = "pref_card_style"
const val CARD_STYLE_DEFAULT = "Default"
const val CARD_STYLE_HORIZONTAL = "Horizontal"
const val CARD_STYLE_GRID = "Grid"

const val PREF_SHOW_GET_MORE_GAMES = "pref_show_get_more_games"
const val PREF_SHOW_LAUNCH_COUNT = "pref_show_launch_count"
const val PREF_AUTO_UPDATES = "pref_auto_updates"

const val PREF_SHOW_USER_NAME = "pref_show_user_name"
const val PREF_USER_NAME = "pref_user_name"

const val PREF_SORT_TYPE = "pref_sort_type"
const val PREF_SHOW_PLAY_TIME = "pref_show_play_time"
const val PREF_STATS_INTERVAL = "pref_stats_interval"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    val activity = context.findActivity()
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    var currentTheme by remember { mutableIntStateOf(prefs.getInt(PREF_THEME, THEME_SYSTEM)) }
    var currentCardStyle by remember { mutableStateOf(prefs.getString(PREF_CARD_STYLE, CARD_STYLE_DEFAULT) ?: CARD_STYLE_DEFAULT) }
    var currentSortType by remember { mutableStateOf(prefs.getString(PREF_SORT_TYPE, "Alphabetical") ?: "Alphabetical") }

    var showGetMoreGames by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_GET_MORE_GAMES, true)) }
    var showLaunchCount by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_LAUNCH_COUNT, true)) }
    var showPlayTime by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_PLAY_TIME, true)) }
    var statsInterval by remember { mutableFloatStateOf(prefs.getFloat(PREF_STATS_INTERVAL, 3f)) }


    var showUserName by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_USER_NAME, true)) }
    var userName by remember { mutableStateOf(prefs.getString(PREF_USER_NAME, "User") ?: "User") }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    val openUpdateDialog = remember { activity?.intent?.getBooleanExtra("EXTRA_OPEN_UPDATE_DIALOG", false) == true }
    var showUpdateDialog by remember { mutableStateOf(openUpdateDialog) }

    var updateStatus by remember { mutableStateOf<UpdateStatus>(UpdateStatus.Idle) }

    val isPixel = remember {
        val brand = Build.BRAND
        val manufacturer = Build.MANUFACTURER
        brand.equals("google", ignoreCase = true) || manufacturer.equals("google", ignoreCase = true)
    }

    val appInfo = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName ?: "1.0"
            val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
            "v$version ($build)"
        } catch (e: Exception) {
            context.getString(R.string.unknown)
        }
    }

    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    LaunchedEffect(openUpdateDialog) {
        if (openUpdateDialog) {
            updateStatus = UpdateStatus.Checking
            val update = Updater.checkForUpdates(currentVersionName)
            updateStatus = if (update != null) UpdateStatus.Available(update) else UpdateStatus.NoUpdate
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.discard)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_header_appearance),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            SettingsItemCard(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.settings_theme_title),
                subtitle = when(currentTheme) {
                    THEME_LIGHT -> stringResource(R.string.settings_theme_light)
                    THEME_DARK -> stringResource(R.string.settings_theme_dark)
                    else -> stringResource(R.string.settings_theme_system)
                },
                containerColor = Color(0xFFfcbd00),
                iconColor = Color(0xFF6d3a01),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Default.ViewAgenda,
                title = stringResource(R.string.settings_card_style_title),
                subtitle = currentCardStyle,
                containerColor = Color(0xFF80da88),
                iconColor = Color(0xFF00522c),
                shape = RoundedCornerShape(4.dp),
                onClick = { showStyleDialog = true }
            )

            Spacer(modifier = Modifier.height(2.dp))

            val nameSwitchShape = if (showUserName) {
                RoundedCornerShape(4.dp)
            } else {
                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
            }

            SettingsSwitchCard(
                icon = Icons.Default.Person,
                title = stringResource(R.string.settings_show_name_title),
                subtitle = stringResource(R.string.settings_show_name_desc),
                containerColor = Color(0xFFffb683),
                iconColor = Color(0xFF753403),
                shape = nameSwitchShape,
                checked = showUserName,
                onCheckedChange = {
                    showUserName = it
                    prefs.edit().putBoolean(PREF_SHOW_USER_NAME, it).apply()
                }
            )

            AnimatedVisibility(
                visible = showUserName,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(2.dp))
                    SettingsItemCard(
                        icon = Icons.Default.Edit,
                        title = stringResource(R.string.settings_edit_name_title),
                        subtitle = userName,
                        containerColor = Color(0xFFe7e0ec),
                        iconColor = Color(0xFF49454f),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                        onClick = { showNameDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_header_preferences),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            SettingsItemCard(
                icon = Icons.Default.Sort,
                title = stringResource(R.string.settings_sort_title),
                subtitle = when(currentSortType) {
                    "Alphabetical" -> stringResource(R.string.sort_alphabetical)
                    "Time" -> stringResource(R.string.sort_playtime)
                    else -> stringResource(R.string.sort_custom)
                },
                containerColor = Color(0xFF67d4ff),
                iconColor = Color(0xFF004e5d),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                onClick = { showSortDialog = true }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsSwitchCard(
                icon = Icons.Default.History,
                title = stringResource(R.string.settings_show_launch_count_title),
                subtitle = stringResource(R.string.settings_show_launch_count_desc),
                containerColor = Color(0xFFd8b9fc),
                iconColor = Color(0xFF5629a4),
                shape = RoundedCornerShape(4.dp),
                checked = showLaunchCount,
                onCheckedChange = {
                    showLaunchCount = it
                    prefs.edit().putBoolean(PREF_SHOW_LAUNCH_COUNT, it).apply()
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            val playTimeShape = if (showPlayTime) {
                RoundedCornerShape(4.dp)
            } else {
                RoundedCornerShape(4.dp)
            }

            SettingsSwitchCard(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.settings_show_playtime_title),
                subtitle = stringResource(R.string.settings_show_playtime_desc),
                containerColor = Color(0xFFffaee4),
                iconColor = Color(0xFF8d0053),
                shape = playTimeShape,
                checked = showPlayTime,
                onCheckedChange = {
                    showPlayTime = it
                    prefs.edit().putBoolean(PREF_SHOW_PLAY_TIME, it).apply()
                }
            )

            AnimatedVisibility(
                visible = showPlayTime,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(2.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFffb3ae)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF8a1a16),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_stats_interval_title),
                                        fontFamily = GoogleSansFlex,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = when(statsInterval.roundToInt()) {
                                            0 -> stringResource(R.string.interval_daily)
                                            1 -> stringResource(R.string.interval_weekly)
                                            2 -> stringResource(R.string.interval_monthly)
                                            else -> stringResource(R.string.interval_yearly)
                                        },
                                        fontFamily = GoogleSansFlex,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Slider(
                                value = statsInterval,
                                onValueChange = {
                                    statsInterval = it
                                    prefs.edit().putFloat(PREF_STATS_INTERVAL, it).apply()
                                },
                                valueRange = 0f..3f,
                                steps = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val getMoreGamesShape = if (isPixel) RoundedCornerShape(4.dp) else RoundedCornerShape(4.dp)

            SettingsSwitchCard(
                icon = Icons.Default.ShoppingBag,
                title = stringResource(R.string.settings_show_get_more_title),
                subtitle = stringResource(R.string.settings_show_get_more_desc),
                containerColor = Color(0xFF67d4ff),
                iconColor = Color(0xFF004e5d),
                shape = getMoreGamesShape,
                checked = showGetMoreGames,
                onCheckedChange = {
                    showGetMoreGames = it
                    prefs.edit().putBoolean(PREF_SHOW_GET_MORE_GAMES, it).apply()
                }
            )

            if (isPixel) {
                Spacer(modifier = Modifier.height(2.dp))

                SettingsItemCard(
                    icon = Icons.Rounded.SportsEsports,
                    title = stringResource(R.string.settings_google_play_title),
                    subtitle = stringResource(R.string.settings_google_play_desc),
                    containerColor = Color(0xFF80da88),
                    iconColor = Color(0xFF00522c),
                    shape = RoundedCornerShape(4.dp),
                    onClick = {
                        try {
                            val intent = Intent()
                            intent.component = ComponentName("com.google.android.gms", "com.google.android.gms.gp.gameservice.SettingsActivity")
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Google Play Games not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Default.Tune,
                title = stringResource(R.string.settings_advanced_title),
                subtitle = stringResource(R.string.settings_advanced_desc),
                containerColor = Color(0xFFC5C0FF),
                iconColor = Color(0xFF2D237A),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                onClick = {
                    val intent = Intent(context, AdvancedSettingsActivity::class.java)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_header_info),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            SettingsItemCard(
                icon = Icons.Default.Info,
                title = stringResource(R.string.settings_version_title),
                subtitle = appInfo,
                containerColor = Color(0xFFa1c9ff),
                iconColor = Color(0xFF0641a0),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                onClick = {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Default.Code,
                title = stringResource(R.string.settings_developer_title),
                subtitle = stringResource(R.string.settings_developer_name),
                containerColor = Color(0xFFc7c7c7),
                iconColor = Color(0xFF474747),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FeDeveloper95"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Default.BugReport,
                title = stringResource(R.string.settings_report_title),
                subtitle = stringResource(R.string.settings_report_desc),
                containerColor = Color(0xFFffb3ae),
                iconColor = Color(0xFF8a1a16),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/fedeveloper95"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            SettingsItemCard(
                icon = Icons.Default.SystemUpdate,
                title = stringResource(R.string.settings_check_updates_title),
                subtitle = stringResource(R.string.settings_check_updates_desc),
                containerColor = Color(0xFF67d4ff),
                iconColor = Color(0xFF004e5d),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                onClick = {
                    showUpdateDialog = true
                    updateStatus = UpdateStatus.Checking
                    scope.launch {
                        val update = Updater.checkForUpdates(currentVersionName)
                        updateStatus = if (update != null) UpdateStatus.Available(update) else UpdateStatus.NoUpdate
                    }
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }


    if (showNameDialog) {
        var tempName by remember { mutableStateOf(userName) }

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val cornerPercent by animateIntAsState(
            targetValue = if (isPressed) 15 else 50,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "btnMorph"
        )

        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_edit_name_title),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = {
                        Text(
                            text = stringResource(R.string.dialog_edit_name_hint),
                            fontFamily = GoogleSansFlex
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = GoogleSansFlex,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            userName = tempName
                            prefs.edit().putString(PREF_USER_NAME, tempName).apply()
                        }
                        showNameDialog = false
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
                TextButton(onClick = { showNameDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (showThemeDialog) {
        ExpressiveSingleChoiceDialog(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.settings_theme_title),
            options = listOf(
                stringResource(R.string.settings_theme_system),
                stringResource(R.string.settings_theme_light),
                stringResource(R.string.settings_theme_dark)
            ),
            selectedIndex = currentTheme,
            onOptionSelected = { index ->
                currentTheme = index
                prefs.edit().putInt(PREF_THEME, index).apply()
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showStyleDialog) {
        val options = listOf(
            stringResource(R.string.settings_card_style_default),
            stringResource(R.string.settings_card_style_horizontal),
            stringResource(R.string.settings_card_style_grid)
        )
        val currentIndex = options.indexOf(currentCardStyle).coerceAtLeast(0)

        ExpressiveSingleChoiceDialog(
            icon = Icons.Default.ViewAgenda,
            title = stringResource(R.string.settings_card_style_title),
            options = options,
            selectedIndex = currentIndex,
            onOptionSelected = { index ->
                val newStyle = when(index) {
                    0 -> CARD_STYLE_DEFAULT
                    1 -> CARD_STYLE_HORIZONTAL
                    else -> CARD_STYLE_GRID
                }
                currentCardStyle = newStyle
                prefs.edit().putString(PREF_CARD_STYLE, newStyle).apply()
                showStyleDialog = false
            },
            onDismiss = { showStyleDialog = false }
        )
    }

    if (showSortDialog) {
        val options = listOf(
            stringResource(R.string.sort_alphabetical),
            stringResource(R.string.sort_playtime),
            stringResource(R.string.sort_custom)
        )
        val currentIndex = when(currentSortType) {
            "Alphabetical" -> 0
            "Time" -> 1
            else -> 2
        }

        ExpressiveSingleChoiceDialog(
            icon = Icons.Default.Sort,
            title = stringResource(R.string.settings_sort_title),
            options = options,
            selectedIndex = currentIndex,
            onOptionSelected = { index ->
                val newSort = when(index) {
                    0 -> "Alphabetical"
                    1 -> "Time"
                    else -> "Custom"
                }
                currentSortType = newSort
                prefs.edit().putString(PREF_SORT_TYPE, newSort).apply()
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }

    if (showUpdateDialog) {
        UpdateDialog(
            status = updateStatus,
            onDismiss = {
                showUpdateDialog = false
                activity?.intent?.removeExtra("EXTRA_OPEN_UPDATE_DIALOG")
            },
            onUpdate = { url ->
                Updater.startDownload(context, url, (updateStatus as UpdateStatus.Available).info.version)
                showUpdateDialog = false
            },
            onCheckAgain = {
                updateStatus = UpdateStatus.Checking
                scope.launch {
                    val update = Updater.checkForUpdates(currentVersionName)
                    updateStatus = if (update != null) UpdateStatus.Available(update) else UpdateStatus.NoUpdate
                }
            }
        )
    }
}

@Composable
fun AnimatedUpdateButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnMorph"
    )

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = stringResource(R.string.update_action),
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun AnimatedCheckAgainButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "btnMorph"
    )

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = stringResource(R.string.settings_check_updates_title),
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateDialog(
    status: UpdateStatus,
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit,
    onCheckAgain: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        when(status) {
                            is UpdateStatus.Error -> MaterialTheme.colorScheme.errorContainer
                            is UpdateStatus.Available -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(status) {
                        is UpdateStatus.Error -> Icons.Default.Error
                        is UpdateStatus.Available -> Icons.Default.SystemUpdate
                        else -> Icons.Default.CloudDownload
                    },
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = when(status) {
                        is UpdateStatus.Error -> MaterialTheme.colorScheme.onErrorContainer
                        is UpdateStatus.Available -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        title = {
            Text(
                text = when(status) {
                    is UpdateStatus.Checking -> stringResource(R.string.update_checking)
                    is UpdateStatus.NoUpdate -> stringResource(R.string.update_no_update)
                    is UpdateStatus.Available -> stringResource(R.string.update_available, status.info.version)
                    is UpdateStatus.Error -> stringResource(R.string.update_error)
                    else -> ""
                },
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when(status) {
                    is UpdateStatus.Checking -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        LoadingIndicator(
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    is UpdateStatus.NoUpdate -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.update_latest_version_msg),
                            fontFamily = GoogleSansFlex,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    is UpdateStatus.Available -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = status.info.changelog,
                                    fontFamily = GoogleSansFlex,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    is UpdateStatus.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.update_error_msg),
                            fontFamily = GoogleSansFlex,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            if (status is UpdateStatus.Available) {
                AnimatedUpdateButton(onClick = { onUpdate(status.info.downloadUrl) })
            } else if (status is UpdateStatus.NoUpdate || status is UpdateStatus.Error) {
                AnimatedCheckAgainButton(onClick = onCheckAgain)
            }
        },
        dismissButton = {
            if (status is UpdateStatus.Available) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.later),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (status is UpdateStatus.NoUpdate || status is UpdateStatus.Error) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.close),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 6.dp
    )
}

@Composable
fun SettingsItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconColor: Color,
    shape: Shape,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingsSwitchCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconColor: Color,
    shape: Shape,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    thumbContent = {
                        if (checked) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )
            },
            modifier = Modifier.padding(vertical = 4.dp),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ExpressiveSingleChoiceDialog(
    icon: ImageVector,
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEachIndexed { index, text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = (index == selectedIndex),
                                onClick = { onOptionSelected(index) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (index == selectedIndex),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = text,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.discard),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 6.dp
    )
}