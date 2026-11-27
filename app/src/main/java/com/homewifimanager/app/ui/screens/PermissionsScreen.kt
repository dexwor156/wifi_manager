package com.homewifimanager.app.ui.screens

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material.icons.rounded.Autorenew
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.homewifimanager.app.permissions.UsageAccessHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.Intent
import com.homewifimanager.app.service.UsageMonitorService

@Composable
fun PermissionsScreen(
    onSetupPermissions: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val wifiPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.NEARBY_WIFI_DEVICES
    } else {
        Manifest.permission.ACCESS_FINE_LOCATION
    }

    var wifiPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                wifiPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val wifiPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            wifiPermissionGranted = granted
        }

    var notificationPermissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            notificationPermissionGranted = granted
        }

    var usageAccessGranted by remember {
        mutableStateOf(UsageAccessHelper.hasUsageAccess(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageAccessGranted =
                    UsageAccessHelper.hasUsageAccess(context)
                // Recheck Wi-Fi permission
                wifiPermissionGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        wifiPermission
                    ) == PackageManager.PERMISSION_GRANTED

                notificationPermissionGranted =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "3 OF 3",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "One last step",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A few permissions help Home WiFi Manager\nwork reliably. You're always in control.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(34.dp))

        PermissionItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null
                )
            },
            title = "Notification access",
            description = if (notificationPermissionGranted) {
                "Monitoring notifications allowed"
            } else {
                "Allow monitoring notifications"
            },
            status = if (notificationPermissionGranted) {
                "Enabled"
            } else {
                "Required"
            }
        )

        PermissionItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Autorenew,
                    contentDescription = null
                )
            },
            title = "Background monitoring",
            description = "Keep tracking when the app is closed",
            status = "Enabled"
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        PermissionItem(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DataUsage,
                    contentDescription = null
                )
            },
            title = "Usage access",
            description = if (usageAccessGranted) {
                "Usage access enabled"
            } else {
                "Monitor data usage on home Wi-Fi"
            },
            status = if (usageAccessGranted) {
                "Enabled"
            } else {
                "Required"
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = "Your usage data stays private on this device.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                when {
                    !usageAccessGranted -> {
                        UsageAccessHelper.openUsageAccessSettings(context)
                    }

                    !wifiPermissionGranted -> {
                        wifiPermissionLauncher.launch(wifiPermission)
                    }

                    !notificationPermissionGranted &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {

                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }

                    else -> {
                        val serviceIntent =
                            Intent(context, UsageMonitorService::class.java).apply {
                                action = UsageMonitorService.ACTION_START
                            }

                        ContextCompat.startForegroundService(
                            context,
                            serviceIntent
                        )

                        onSetupPermissions()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp
            )
        ) {
            Text(
                text = "Set up permissions",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun PermissionItem(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    status: String = "Required"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(11.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.primary
                ) {
                    icon()
                }
            }
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}