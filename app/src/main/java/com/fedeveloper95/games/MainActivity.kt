package com.fedeveloper95.games

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

// --- Data Model ---
data class GameApp(
    val packageName: String,
    val name: String,
    val icon: Any? // Can be Drawable, Uri, or null
)

// --- Font Configuration ---
val GoogleSansFlex = FontFamily(
    Font(
        R.font.sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f) // Max Roundness
        )
    )
)

// --- ViewModel ---
class GameViewModel(private val packageManager: PackageManager, private val prefs: android.content.SharedPreferences) : ViewModel() {

    private val _installedGames = MutableStateFlow<List<GameApp>>(emptyList())
    private val _manualGames = MutableStateFlow<Set<String>>(emptySet())
    private val _hiddenGames = MutableStateFlow<Set<String>>(emptySet())
    private val _gameOrder = MutableStateFlow<List<String>>(emptyList())

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private var loadJob: Job? = null

    val games: StateFlow<List<GameApp>> = combine(
        _installedGames,
        _manualGames,
        _hiddenGames,
        _gameOrder,
        _searchQuery
    ) { installed, manual, hidden, order, query ->
        val allGames = installed.filter {
            (it.packageName !in hidden) || (it.packageName in manual && it.packageName !in hidden)
        }.toMutableList()

        // Apply Ordering
        val orderedGames = if (order.isNotEmpty()) {
            val orderMap = order.withIndex().associate { it.value to it.index }
            allGames.sortedBy { orderMap[it.packageName] ?: Int.MAX_VALUE }
        } else {
            allGames.sortedBy { it.name }
        }

        // Apply Search Filter
        if (query.isBlank()) {
            orderedGames
        } else {
            orderedGames.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInstalledApps = MutableStateFlow<List<GameApp>>(emptyList())

    init {
        loadPreferences()
        loadGames()
    }

    private fun loadPreferences() {
        _manualGames.value = prefs.getStringSet("manual_games", emptySet()) ?: emptySet()
        _hiddenGames.value = prefs.getStringSet("hidden_games", emptySet()) ?: emptySet()

        val orderString = prefs.getString("game_order", null)
        if (orderString != null) {
            _gameOrder.value = orderString.split(",").filter { it.isNotEmpty() }
        }
    }

    private fun saveOrder() {
        val orderString = _gameOrder.value.joinToString(",")
        prefs.edit().putString("game_order", orderString).apply()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun loadGames() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val allApps = packageManager.queryIntentActivities(intent, 0)

            val gameApps = mutableListOf<GameApp>()
            val allAppsList = mutableListOf<GameApp>()

            for (resolveInfo in allApps) {
                val appInfo = resolveInfo.activityInfo.applicationInfo ?: continue
                val packageName = resolveInfo.activityInfo.packageName
                val name = resolveInfo.loadLabel(packageManager).toString()

                // Fix for high-res icons
                val icon = try {
                    resolveInfo.activityInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    appInfo.loadIcon(packageManager)
                }

                val game = GameApp(packageName, name, icon)
                allAppsList.add(game)

                val isGame = (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0 ||
                        (appInfo.category == ApplicationInfo.CATEGORY_GAME) ||
                        _manualGames.value.contains(packageName)

                if (isGame) {
                    gameApps.add(game)
                }
            }
            _installedGames.value = gameApps
            allInstalledApps.value = allAppsList.sortedBy { it.name }
        }
    }

    fun addManualGame(packageName: String) {
        val current = _manualGames.value.toMutableSet()
        current.add(packageName)
        _manualGames.value = current
        prefs.edit().putStringSet("manual_games", current).apply()

        // Remove from hidden if it was there
        val currentHidden = _hiddenGames.value.toMutableSet()
        if (currentHidden.contains(packageName)) {
            currentHidden.remove(packageName)
            _hiddenGames.value = currentHidden
            prefs.edit().putStringSet("hidden_games", currentHidden).apply()
        }

        loadGames()
    }

    fun hideGame(packageName: String) {
        val current = _hiddenGames.value.toMutableSet()
        current.add(packageName)
        _hiddenGames.value = current
        prefs.edit().putStringSet("hidden_games", current).apply()

        // Also remove from manual if present to be clean
        val currentManual = _manualGames.value.toMutableSet()
        if (currentManual.contains(packageName)) {
            currentManual.remove(packageName)
            _manualGames.value = currentManual
            prefs.edit().putStringSet("manual_games", currentManual).apply()
        }
        loadGames()
    }

    fun updateGameOrder(fromIndex: Int, toIndex: Int) {
        val currentList = games.value.map { it.packageName }.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _gameOrder.value = currentList
            saveOrder()
        }
    }
}

class GameViewModelFactory(private val pm: PackageManager, private val prefs: android.content.SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(pm, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- UI Components ---

@Composable
fun GameHubTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        androidx.compose.material3.dynamicDarkColorScheme(LocalContext.current)
    } else {
        androidx.compose.material3.dynamicLightColorScheme(LocalContext.current)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as ComponentActivity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(), // System font
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHubScreen(viewModel: GameViewModel) {
    val games by viewModel.games.collectAsState()
    val allApps by viewModel.allInstalledApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var gameToRemove by remember { mutableStateOf<GameApp?>(null) }
    var isEditMode by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Reorderable State
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            viewModel.updateGameOrder(from.index - 1, to.index - 1) // -1 because of Spacer/Search header
        }
    )

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Game Hub",
                        fontFamily = GoogleSansFlex,
                        maxLines = 1
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Game")
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(state)
                    .detectReorderAfterLongPress(state),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sottotitolo e Barra di Ricerca come header
                item {
                    Column {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Risultati ricerca" else "${games.size} giochi pronti",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // SEARCH BAR
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            placeholder = { Text("Cerca un gioco...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            } else null,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            singleLine = true
                        )
                    }
                }

                items(games, key = { it.packageName }) { game ->
                    ReorderableItem(state, key = game.packageName) { isDragging ->
                        val elevation = animateFloatAsState(if (isDragging) 8f else 0f)

                        SwipeToDeleteContainer(
                            item = game,
                            onDelete = { gameToRemove = game },
                            // Enabled only if NOT dragging and NOT searching
                            enabled = !isDragging && searchQuery.isEmpty()
                        ) {
                            GameListItem(
                                game = game,
                                isDragging = isDragging,
                                modifier = Modifier
                            )
                        }
                    }
                }

                item {
                    GetMoreGamesCard()
                }
            }
        }
    }

    if (showAddDialog) {
        AddGameBottomSheet(
            allApps = allApps,
            onDismiss = { showAddDialog = false },
            onAddGame = {
                viewModel.addManualGame(it.packageName)
                showAddDialog = false
            }
        )
    }

    if (gameToRemove != null) {
        AlertDialog(
            onDismissRequest = { gameToRemove = null },
            title = { Text("Rimuovi gioco") },
            text = { Text("Vuoi nascondere ${gameToRemove?.name} dal Game Hub?") },
            confirmButton = {
                Button(onClick = {
                    gameToRemove?.let { viewModel.hideGame(it.packageName) }
                    gameToRemove = null
                }) {
                    Text("Rimuovi")
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToRemove = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameListItem(
    game: GameApp,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "scale"
    )

    val shapeValue by animateFloatAsState(
        targetValue = if (isPressed) 12f else 28f, // Morphing shape
        animationSpec = tween(300),
        label = "shape"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scaleIn(initialScale = 0.9f)
            .combinedClickable(
                onClick = {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    } else {
                        Toast.makeText(context, "Impossibile avviare", Toast.LENGTH_SHORT).show()
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // Reorder logic handled by library
                }
            ),
        shape = RoundedCornerShape(shapeValue.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex,
                    maxLines = 1
                )
            }

            if (isDragging) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Move",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                PlayButton()
            }
        }
    }
}

@Composable
fun PlayButton() {
    var isPressed by remember { mutableStateOf(false) }

    // Pill to Squircle animation
    val cornerRadius by animateFloatAsState(
        targetValue = if (isPressed) 15f else 50f,
        label = "buttonShape"
    )

    Button(
        onClick = { /* Handled by Card click usually, visual only here or secondary action */ },
        shape = RoundedCornerShape(percent = cornerRadius.toInt()),
        modifier = Modifier.height(40.dp),
        // Interaction source to detect press state would be needed for full effect inside parent clickable
    ) {
        Text("Gioca")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    item: GameApp,
    onDelete: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val currentItem by remember { mutableStateOf(item) }

    // FIX SWIPE SENSITIVITY: Set 50% threshold
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                return@rememberSwipeToDismissBoxState true
            }
            false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.50f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(28.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        content = { content() },
        enableDismissFromStartToEnd = false,
        gesturesEnabled = enabled
    )
}

@Composable
fun GetMoreGamesCard() {
    val context = LocalContext.current

    Card(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/category/GAME")
            }
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scarica nuovi giochi", fontFamily = GoogleSansFlex)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameBottomSheet(
    allApps: List<GameApp>,
    onDismiss: () -> Unit,
    onAddGame: (GameApp) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.navigationBarsPadding() // Edge to edge fix
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Aggiungi alla libreria",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = GoogleSansFlex,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cerca app...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            val filteredApps = allApps.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        filteredApps.forEachIndexed { index, app ->
                            GroupedAppItem(
                                app = app,
                                onClick = { onAddGame(app) },
                                isFirst = index == 0,
                                isLast = index == filteredApps.size - 1
                            )
                            if (index < filteredApps.size - 1) {
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupedAppItem(
    app: GameApp,
    onClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    // Custom shape logic for grouped look
    val topRadius = if (isFirst) 24.dp else 4.dp
    val bottomRadius = if (isLast) 24.dp else 4.dp

    ListItem(
        headlineContent = { Text(app.name, fontFamily = GoogleSansFlex) },
        leadingContent = {
            AsyncImage(
                model = app.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        },
        trailingContent = { Icon(Icons.Default.Add, contentDescription = null) },
        modifier = Modifier
            .clip(RoundedCornerShape(
                topStart = topRadius, topEnd = topRadius,
                bottomStart = bottomRadius, bottomEnd = bottomRadius
            ))
            .combinedClickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// --- Main Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            GameViewModelFactory(packageManager, getSharedPreferences("game_hub_prefs", MODE_PRIVATE))
        )[GameViewModel::class.java]

        setContent {
            GameHubTheme {
                GameHubScreen(viewModel = viewModel)
            }
        }
    }
}