package com.fedeveloper95.games

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

// --- COSTANTI PREFERENZE ---
const val PREF_THEME = "pref_theme"
const val THEME_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

const val PREF_CARD_STYLE = "pref_card_style"
const val CARD_STYLE_DEFAULT = "Default"
const val CARD_STYLE_HORIZONTAL = "Horizontal"
const val CARD_STYLE_GRID = "Grid"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    // Stati delle impostazioni
    var currentTheme by remember { mutableIntStateOf(prefs.getInt(PREF_THEME, THEME_SYSTEM)) }
    var currentCardStyle by remember { mutableStateOf(prefs.getString(PREF_CARD_STYLE, CARD_STYLE_DEFAULT) ?: CARD_STYLE_DEFAULT) }

    // Stati dei dialoghi
    var showThemeDialog by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }

    // Info App
    val appInfo = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName
            val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
            "v$version ($build)"
        } catch (e: Exception) {
            "Sconosciuto"
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
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
            // SEZIONE ASPETTO
            SettingsHeader("Aspetto")

            SettingsItem(
                icon = Icons.Outlined.DarkMode,
                title = "Tema",
                subtitle = when(currentTheme) {
                    THEME_LIGHT -> "Chiaro"
                    THEME_DARK -> "Scuro"
                    else -> "Predefinito di sistema"
                },
                onClick = { showThemeDialog = true }
            )

            SettingsItem(
                icon = Icons.Outlined.ViewAgenda,
                title = "Stile Card",
                subtitle = currentCardStyle,
                onClick = { showStyleDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            // SEZIONE INFO
            SettingsHeader("Informazioni")

            SettingsItem(
                icon = Icons.Default.Info,
                title = "Versione App",
                subtitle = appInfo,
                onClick = {} // Non fa nulla
            )

            SettingsItem(
                icon = Icons.Default.Code,
                title = "Sviluppatore",
                subtitle = "FeDeveloper95",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FeDeveloper95"))
                    context.startActivity(intent)
                }
            )
        }
    }

    // DIALOGHI
    if (showThemeDialog) {
        SingleChoiceDialog(
            title = "Scegli il tema",
            options = listOf("Predefinito di sistema", "Chiaro", "Scuro"),
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
        val options = listOf(CARD_STYLE_DEFAULT, CARD_STYLE_HORIZONTAL, CARD_STYLE_GRID)
        val currentIndex = options.indexOf(currentCardStyle).coerceAtLeast(0)

        SingleChoiceDialog(
            title = "Scegli stile card",
            options = options,
            selectedIndex = currentIndex,
            onOptionSelected = { index ->
                val newStyle = options[index]
                currentCardStyle = newStyle
                prefs.edit().putString(PREF_CARD_STYLE, newStyle).apply()
                showStyleDialog = false
            },
            onDismiss = { showStyleDialog = false }
        )
    }
}

// --- COMPONENTI UI ---

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SingleChoiceDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEachIndexed { index, text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
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
                            onClick = null // Gestito dalla Row
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}