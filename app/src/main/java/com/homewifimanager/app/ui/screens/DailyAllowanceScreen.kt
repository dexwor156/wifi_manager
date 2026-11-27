package com.homewifimanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyAllowanceScreen(
    onContinue: (Double) -> Unit = {}
) {
    var allowance by remember { mutableDoubleStateOf(2.0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "2 OF 3",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Daily allowance",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose how much data can be used between 8 AM and midnight.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Main allowance selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            AllowanceCircleButton(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Decrease"
                    )
                },
                onClick = {
                    if (allowance > 0.5) {
                        allowance -= 0.5
                    }
                }
            )

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%.2f", allowance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "GB",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }

            AllowanceCircleButton(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Increase"
                    )
                },
                onClick = {
                    if (allowance < 50.0) {
                        allowance += 0.5
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Preset amounts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            AllowancePreset(
                text = "1 GB",
                selected = allowance == 1.0,
                modifier = Modifier.weight(1f)
            ) {
                allowance = 1.0
            }

            AllowancePreset(
                text = "2 GB",
                selected = allowance == 2.0,
                modifier = Modifier.weight(1f)
            ) {
                allowance = 2.0
            }

            AllowancePreset(
                text = "3 GB",
                selected = allowance == 3.0,
                modifier = Modifier.weight(1f)
            ) {
                allowance = 3.0
            }

            AllowancePreset(
                text = "5 GB",
                selected = allowance == 5.0,
                modifier = Modifier.weight(1f)
            ) {
                allowance = 5.0
            }

            AllowancePreset(
                text = "Custom",
                selected = allowance !in listOf(1.0, 2.0, 3.0, 5.0),
                modifier = Modifier.weight(1.25f)
            ) {
                // Custom entry screen/dialog later
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )

        ScheduleRow(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = "Metered",
            time = "8:00 AM – 12:00 AM"
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )

        ScheduleRow(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Nightlight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            title = "Free",
            time = "12:00 AM – 8:00 AM"
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onContinue(allowance)
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
                text = "Continue",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun AllowanceCircleButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(46.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                Box(
                    modifier = Modifier.size(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
        }
    }
}

@Composable
private fun AllowancePreset(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(9.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (selected) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun ScheduleRow(
    icon: @Composable () -> Unit,
    title: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(11.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(9.dp)
            ) {
                icon()
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}