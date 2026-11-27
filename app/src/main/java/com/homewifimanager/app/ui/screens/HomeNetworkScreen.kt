package com.homewifimanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.homewifimanager.app.network.WifiNetworkManager
import com.homewifimanager.app.permissions.WifiPermissionHelper

@Composable
fun HomeNetworkScreen(
    networkName: String = "Home WiFi",
    onUseNetwork: (String) -> Unit = {},
    onChooseAnotherNetwork: () -> Unit = {}
) {
    val context = LocalContext.current

    val wifiNetworkManager = remember {
        WifiNetworkManager(context.applicationContext)
    }

    var detectedNetwork by remember {
        mutableStateOf<String?>(null)
    }

    var hasWifiPermission by remember {
        mutableStateOf(
            WifiPermissionHelper.hasRequiredPermissions(context)
        )
    }

    fun refreshWifi() {
        hasWifiPermission =
            WifiPermissionHelper.hasRequiredPermissions(context)

        detectedNetwork = if (hasWifiPermission) {
            wifiNetworkManager.getCurrentSsid()
        } else {
            null
        }
    }

    val wifiPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            refreshWifi()
        }

    LaunchedEffect(Unit) {
        refreshWifi()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
    ) {

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "1 OF 3",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Home network",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose the Wi-Fi network you want this app to\nmonitor.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(34.dp))

        // Selected network
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when {
                            !hasWifiPermission -> "Wi-Fi permission required"
                            detectedNetwork != null -> detectedNetwork!!
                            else -> "Wi-Fi unavailable"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    Color(0xFF43B77A),
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Information
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Only usage on your selected home Wi-Fi will count\ntoward your allowance.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                when {
                    !hasWifiPermission -> {
                        wifiPermissionLauncher.launch(
                            WifiPermissionHelper.requiredPermissions()
                        )
                    }

                    detectedNetwork != null -> {
                        onUseNetwork(detectedNetwork!!)
                    }

                    else -> {
                        refreshWifi()
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
                text = "Use this network",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        TextButton(
            onClick = onChooseAnotherNetwork,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 2.dp)
        ) {
            Text(
                text = "Choose another network",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}