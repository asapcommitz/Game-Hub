package com.fedeveloper95.games

import android.Manifest
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SwipeRight
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class OnboardingPageInfo(
    val content: @Composable (onUpdateScrollState: (Boolean) -> Unit) -> Unit
)

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)

        if (!isFirstRun) {
            finishOnboarding()
            return
        }

        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomePagerScreen(onFinished = {
                        prefs.edit().putBoolean("is_first_run", false).apply()
                        finishOnboarding()
                    })
                }
            }
        }
    }

    private fun finishOnboarding() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
fun WelcomePagerScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val commonAnimSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

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

    val thinHeaderStyle = TextStyle(
        fontFamily = customWelcomeFontFamily,
        fontSize = 48.sp
    )

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var canInstallPackages by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else true
        )
    }

    var hasUsageStatsPermission by remember {
        mutableStateOf(checkUsageStatsPermission(context))
    }

    var isLastPageScrolledToEnd by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    canInstallPackages = context.packageManager.canRequestPackageInstalls()
                }
                hasUsageStatsPermission = checkUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    val installParamsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canInstallPackages = context.packageManager.canRequestPackageInstalls()
        }
    }

    val pages = listOf(
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    Text(
                        text = stringResource(R.string.welcome_to),
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 56.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RotatingShapeContainer(
                            modifier = Modifier.size(280.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = stringResource(R.string.welcome_preparing_subtitle),
                            fontFamily = GoogleSansFlex,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(1.2f))
                }
            }
        ),
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    Text(
                        text = stringResource(R.string.perm_required),
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.perm_permissions),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 56.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.perm_intro_text),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = GoogleSansFlex
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column {
                            PermissionRow(
                                icon = Icons.Rounded.Notifications,
                                iconColor = Color(0xFFffaee4),
                                iconTint = Color(0xFF8d0053),
                                title = stringResource(R.string.perm_notif_title),
                                description = stringResource(R.string.perm_notif_desc),
                                control = {
                                    Switch(
                                        checked = hasNotificationPermission,
                                        onCheckedChange = {
                                            if (hasNotificationPermission) {
                                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                }
                                                context.startActivity(intent)
                                            } else {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            }
                                        },
                                        thumbContent = {
                                            Icon(
                                                imageVector = if (hasNotificationPermission) Icons.Rounded.Check else Icons.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                },
                                onClick = {
                                    if (hasNotificationPermission) {
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                }
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            PermissionRow(
                                icon = Icons.Rounded.Timer,
                                iconColor = Color(0xFFd8b9fc),
                                iconTint = Color(0xFF5629a4),
                                title = stringResource(R.string.feat_history_title),
                                description = stringResource(R.string.perm_usage_desc),
                                control = {
                                    Icon(
                                        imageVector = if (hasUsageStatsPermission) Icons.Rounded.Check else Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = if (hasUsageStatsPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    if (!hasUsageStatsPermission) {
                                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                        context.startActivity(intent)
                                    }
                                }
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            PermissionRow(
                                icon = Icons.Rounded.SystemUpdate,
                                iconColor = Color(0xFFffb683),
                                iconTint = Color(0xFF753403),
                                title = stringResource(R.string.perm_install_title),
                                description = stringResource(R.string.perm_install_desc),
                                control = {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        installParamsLauncher.launch(intent)
                                    }
                                }
                            )

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            PermissionRow(
                                icon = Icons.Rounded.SportsEsports,
                                iconColor = Color(0xFF67d4ff),
                                iconTint = Color(0xFF004e5d),
                                title = stringResource(R.string.perm_dashboard_title),
                                description = stringResource(R.string.perm_dashboard_desc),
                                control = {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
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
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        ),
        OnboardingPageInfo(
            content = { onUpdateScroll ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = stringResource(R.string.feat_discover),
                            style = thinHeaderStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.feat_features),
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 56.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.feat_intro),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = GoogleSansFlex
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        val scrollState = rememberScrollState()

                        val isAtBottom by remember {
                            derivedStateOf {
                                val layoutInfo = scrollState.maxValue
                                layoutInfo == 0 || scrollState.value >= (layoutInfo - 20)
                            }
                        }

                        LaunchedEffect(isAtBottom) {
                            onUpdateScroll(isAtBottom)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column {
                                    FeatureItem(
                                        icon = Icons.Rounded.DragHandle,
                                        iconColor = Color(0xFFfcbd00),
                                        iconTint = Color(0xFF6d3a01),
                                        title = stringResource(R.string.feat_order_title),
                                        description = stringResource(R.string.feat_order_desc)
                                    )

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    FeatureItem(
                                        icon = Icons.Rounded.GridView,
                                        iconColor = Color(0xFF80da88),
                                        iconTint = Color(0xFF00522c),
                                        title = stringResource(R.string.feat_layout_title),
                                        description = stringResource(R.string.feat_layout_desc)
                                    )

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    FeatureItem(
                                        icon = Icons.Rounded.Person,
                                        iconColor = Color(0xFFffb683),
                                        iconTint = Color(0xFF753403),
                                        title = stringResource(R.string.feat_name_title),
                                        description = stringResource(R.string.feat_name_desc)
                                    )

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    FeatureItem(
                                        icon = Icons.Rounded.SwipeRight,
                                        iconColor = Color(0xFFffb3ae),
                                        iconTint = Color(0xFF8a1a16),
                                        title = stringResource(R.string.feat_manage_title),
                                        description = stringResource(R.string.feat_manage_desc)
                                    )

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    FeatureItem(
                                        icon = Icons.Rounded.ShoppingBag,
                                        iconColor = Color(0xFFffaee4),
                                        iconTint = Color(0xFF8d0053),
                                        title = stringResource(R.string.feat_store_title),
                                        description = stringResource(R.string.feat_store_desc)
                                    )

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    FeatureItem(
                                        icon = Icons.Rounded.History,
                                        iconColor = Color(0xFFd8b9fc),
                                        iconTint = Color(0xFF5629a4),
                                        title = stringResource(R.string.feat_history_title),
                                        description = stringResource(R.string.feat_history_desc)
                                    )

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    FeatureItem(
                                        icon = Icons.Rounded.SystemUpdate,
                                        iconColor = Color(0xFF67d4ff),
                                        iconTint = Color(0xFF004e5d),
                                        title = stringResource(R.string.feat_update_title),
                                        description = stringResource(R.string.feat_update_desc)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(100.dp))
                        }

                        this@Column.AnimatedVisibility(
                            visible = scrollState.canScrollForward,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            LargeFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isFirstPage = pagerState.currentPage == 0
    val isLastPage = pagerState.currentPage == pages.size - 1

    BackHandler(enabled = !isFirstPage) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1, animationSpec = commonAnimSpec)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { index ->
            OnboardingPageItem(
                page = pages[index],
                onUpdateScroll = { scrolledToEnd ->
                    if (index == pages.size - 1) {
                        isLastPageScrolledToEnd = scrolledToEnd
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val backButtonWeight by animateFloatAsState(
            targetValue = if (isFirstPage) 0.0001f else 1f,
            animationSpec = commonAnimSpec,
            label = "backWeight"
        )

        val spacerWeight by animateFloatAsState(
            targetValue = if (isFirstPage) 0.0001f else 0.05f,
            animationSpec = commonAnimSpec,
            label = "spacerWeight"
        )

        val alphaBack by animateFloatAsState(
            targetValue = if (isFirstPage) 0f else 1f,
            animationSpec = commonAnimSpec,
            label = "backAlpha"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(backButtonWeight)
                    .fillMaxHeight()
                    .alpha(alphaBack)
            ) {
                ExpressiveButton(
                    text = stringResource(R.string.back),
                    onClick = {
                        if (!isFirstPage) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1, animationSpec = commonAnimSpec)
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(spacerWeight))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val isNextEnabled = !isLastPage || isLastPageScrolledToEnd

                val alphaNext by animateFloatAsState(
                    targetValue = if (isNextEnabled) 1f else 0.5f,
                    label = "nextAlpha"
                )

                ExpressiveButton(
                    text = if (isLastPage) stringResource(R.string.get_started) else stringResource(R.string.next),
                    onClick = {
                        if (isLastPage) {
                            if (isLastPageScrolledToEnd) onFinished()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1, animationSpec = commonAnimSpec)
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = alphaNext),
                    contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = alphaNext),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun RotatingShapeContainer(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shapeRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(rotation) {
                val cx = size.width / 2
                val cy = size.height / 2
                val radius = size.minDimension / 2f
                val outerRadius = radius * 0.95f
                val innerRadius = radius * 0.75f
                val numPoints = 12

                val steps = 360
                val angleStep = (2 * PI / steps).toFloat()

                val path = Path()
                for (i in 0 until steps) {
                    val theta = i * angleStep
                    val r = (outerRadius + innerRadius) / 2 + (outerRadius - innerRadius) / 2 * cos(numPoints * theta).toFloat()

                    val x = cx + r * cos(theta).toFloat()
                    val y = cy + r * sin(theta).toFloat()

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()

                drawPath(
                    path = path,
                    color = primaryColor
                )
            }
        }

        Icon(
            imageVector = Icons.Rounded.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(130.dp),
            tint = backgroundColor
        )
    }
}

@Composable
fun PermissionRow(
    icon: ImageVector,
    iconColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    control: @Composable () -> Unit,
    onClick: () -> Unit
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
            Text(
                text = description,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        trailingContent = control,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun FeatureItem(
    icon: ImageVector,
    iconColor: Color,
    iconTint: Color,
    title: String,
    description: String
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
            Text(
                text = description,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun OnboardingPageItem(
    page: OnboardingPageInfo,
    onUpdateScroll: (Boolean) -> Unit
) {
    page.content(onUpdateScroll)
}

@Composable
fun ExpressiveButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnMorph"
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
        }
    }
}