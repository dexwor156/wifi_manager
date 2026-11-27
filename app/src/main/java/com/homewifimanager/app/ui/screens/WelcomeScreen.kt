package com.homewifimanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(0.58f))

        // Wi-Fi illustration
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {

            // Outer ring
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )

            // Middle ring
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        shape = CircleShape
                    )
            )

            // Inner ring
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )

            // Left orbit dot
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 14.dp, y = 7.dp)
                    .size(6.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.70f),
                        CircleShape
                    )
            )

            // Upper-right orbit dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-27).dp, y = 31.dp)
                    .size(4.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                        CircleShape
                    )
            )

            // Main purple tile
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(21.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    )
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(21.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = "Wi-Fi",
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Take control of your\nWi-Fi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Keep track of your daily home Wi-Fi usage\nwithout counting your free hours.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "☾  Free usage every day from 12 AM to 8 AM",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}