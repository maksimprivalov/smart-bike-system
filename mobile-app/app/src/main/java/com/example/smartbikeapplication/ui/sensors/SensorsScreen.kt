package com.example.smartbikeapplication.ui.sensors

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartbikeapplication.data.model.SensorResponse
import com.example.smartbikeapplication.ui.main.BtStatus
import com.example.smartbikeapplication.ui.main.SensorViewModel

private const val BIKE_MAC = "2C:CF:67:20:C9:E0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    onOpenMap: (lat: Double, lon: Double) -> Unit
) {
    val viewModel: SensorViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val status by viewModel.status.collectAsState()
    val signalLost by viewModel.signalLost.collectAsState()

    val context = LocalContext.current
    val activity = context as? android.app.Activity

    var permissionDeniedPermanently by remember { mutableStateOf(false) }

    // On Android 12+ BLUETOOTH_CONNECT is a runtime permission.
    // On Android 11 and below, BLUETOOTH is a normal permission (granted at install).
    val btPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun isBtGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // granted automatically on Android 11 and below
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.BLUETOOTH_CONNECT] == true) {
            permissionDeniedPermanently = false
            viewModel.startBluetooth(context.applicationContext, BIKE_MAC)
        } else {
            // permanently denied = denied + shouldShowRationale is false
            val canAskAgain = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it, Manifest.permission.BLUETOOTH_CONNECT
                )
            } ?: false
            permissionDeniedPermanently = !canAskAgain
        }
    }

    LaunchedEffect(Unit) {
        if (isBtGranted()) {
            viewModel.startBluetooth(context.applicationContext, BIKE_MAC)
        } else {
            permissionLauncher.launch(btPermissions)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("SmartBike", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    ConnectionChip(status)
                    Spacer(Modifier.width(12.dp))
                }
            )
        }
    ) { padding ->
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        val waitingContent = @Composable {
            WaitingContent(
                status = status,
                permissionDeniedPermanently = permissionDeniedPermanently,
                onRetry = {
                    if (isBtGranted()) viewModel.retry()
                    else permissionLauncher.launch(btPermissions)
                },
                onOpenSettings = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            )
        }

        val mapButton = @Composable {
            Button(
                onClick = { onOpenMap(state!!.gps.lat, state!!.gps.lon) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Map, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Map", fontWeight = FontWeight.SemiBold)
            }
        }

        if (isLandscape) {
            // ── Landscape layout ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left column: Speed + GPS + System
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    if (state == null) {
                        waitingContent()
                    } else {
                        SpeedCard(state!!)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GpsCard(state!!, modifier = Modifier.weight(1f))
                            SystemCard(state!!, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // Right column: Trip + Controls + Map
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    if (state != null) {
                        TripCard(state!!)
                        ControlsCard(viewModel)
                        mapButton()
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        } else {
            // ── Portrait layout ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                if (state == null) {
                    waitingContent()
                } else {
                    SpeedCard(state!!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GpsCard(state!!, modifier = Modifier.weight(1f))
                        SystemCard(state!!, modifier = Modifier.weight(1f))
                    }
                    TripCard(state!!)
                    ControlsCard(viewModel)
                    mapButton()
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Signal lost / connection lost banner
    val showBanner = signalLost || status == BtStatus.FAILED
    val bannerText = when {
        status == BtStatus.FAILED -> "Connection lost"
        signalLost -> "No signal from Raspberry Pi..."
        else -> ""
    }
    AnimatedVisibility(
        visible = showBanner && state != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        SignalLostBanner(
            text = bannerText,
            isDisconnected = status == BtStatus.FAILED,
            onRetry = { viewModel.retry() }
        )
    }
    } // end Box
}

@Composable
private fun SignalLostBanner(
    text: String,
    isDisconnected: Boolean,
    onRetry: () -> Unit
) {
    val color = if (isDisconnected) Color(0xFFF44336) else Color(0xFFFF9800)
    val icon = if (isDisconnected) Icons.Filled.BluetoothDisabled else Icons.Filled.SignalWifiStatusbarConnectedNoInternet4

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onRetry,
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
        ) {
            Text("Retry", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConnectionChip(status: BtStatus) {
    val color = when (status) {
        BtStatus.CONNECTED -> Color(0xFF4CAF50)
        BtStatus.CONNECTING -> Color(0xFFFFC107)
        BtStatus.FAILED -> Color(0xFFF44336)
        BtStatus.DEMO -> Color(0xFF42A5F5)
        BtStatus.IDLE -> Color(0xFF9E9E9E)
    }
    val icon: ImageVector = when (status) {
        BtStatus.CONNECTED -> Icons.Filled.BluetoothConnected
        BtStatus.FAILED -> Icons.Filled.BluetoothDisabled
        BtStatus.DEMO -> Icons.Filled.PlayArrow
        else -> Icons.Filled.Bluetooth
    }
    val label = when (status) {
        BtStatus.CONNECTED -> "Connected"
        BtStatus.CONNECTING -> "Connecting..."
        BtStatus.FAILED -> "Failed"
        BtStatus.DEMO -> "Demo"
        BtStatus.IDLE -> "Idle"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = if (status == BtStatus.CONNECTING) 0.25f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(
                    color.copy(alpha = if (status == BtStatus.CONNECTING) dotAlpha else 1f)
                )
        )
        Spacer(Modifier.width(6.dp))
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WaitingContent(
    status: BtStatus,
    permissionDeniedPermanently: Boolean,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (permissionDeniedPermanently) {
                Icon(
                    Icons.Filled.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = Color(0xFFF44336)
                )
                Text(
                    "Bluetooth permission required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF44336)
                )
                Text(
                    "Permission was denied. Open Settings and enable\nBluetooth permissions manually.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Settings")
                }
                return@Box
            }

            when (status) {
                BtStatus.CONNECTING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Text(
                        "Connecting to Raspberry Pi...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BtStatus.FAILED -> {
                    Icon(
                        Icons.Filled.BluetoothDisabled,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = Color(0xFFF44336)
                    )
                    Text(
                        "Connection failed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                    Text(
                        "Make sure the Raspberry Pi is nearby\nand the server is running",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = onRetry) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry")
                    }
                }
                BtStatus.IDLE -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Text(
                        "Requesting Bluetooth permission...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedCard(state: SensorResponse) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "SPEED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "%.1f".format(state.speed.current),
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 60.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "km/h",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpeedStat(label = "AVG", value = "%.1f".format(state.speed.average))
                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SpeedStat(label = "MAX", value = "%.1f".format(state.speed.max))
            }
        }
    }
}

@Composable
private fun SpeedStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "$value km/h",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GpsCard(state: SensorResponse, modifier: Modifier = Modifier) {
    val fixColor = if (state.gps.fix) Color(0xFF4CAF50) else Color(0xFFF44336)
    val fixLabel = if (state.gps.fix) "Fix OK" else "No fix"

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = fixColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "GPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    fixLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = fixColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            DataRow(label = "LAT", value = "%.4f°".format(state.gps.lat))
            DataRow(label = "LON", value = "%.4f°".format(state.gps.lon))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.GpsFixed,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${state.gps.satellites} sat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SystemCard(state: SensorResponse, modifier: Modifier = Modifier) {
    val headlightColor =
        if (state.system.headlight) Color(0xFFFFC107)
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

    val turnLabel = when (state.system.turn_signal) {
        "left" -> "← Left"
        "right" -> "Right →"
        else -> "— None"
    }

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Tune,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "SYSTEM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = headlightColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.system.headlight) "Light ON" else "Light OFF",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (state.system.headlight) Color(0xFFFFC107)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    state.system.headlight_mode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DataRow(label = "TURN", value = turnLabel)
            DataRow(label = "LUX", value = "${state.system.light_level}")
        }
    }
}

@Composable
private fun TripCard(state: SensorResponse) {
    val durationSec = state.trip.duration_sec
    val minutes = durationSec / 60
    val seconds = durationSec % 60

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Route,
                contentDescription = null,
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "TRIP",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.2f km".format(state.trip.distance_km),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "%02d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ControlsCard(viewModel: SensorViewModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "CONTROLS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Turn signals + Horn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.sendTurnLeft() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Left", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.sendTurnOff() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Off", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.sendTurnRight() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Right", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            // Horn
            Button(
                onClick = { viewModel.sendHorn() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Horn", fontWeight = FontWeight.Medium)
            }

            // Headlight mode
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "Headlight",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("auto", "on", "off").forEach { mode ->
                    OutlinedButton(
                        onClick = { viewModel.sendHeadlightMode(mode) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(mode.replaceFirstChar { it.uppercase() }, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
