package com.homewifimanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.homewifimanager.app.navigation.AppNavigation
import com.homewifimanager.app.ui.theme.HomeWiFiManagerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            HomeWiFiManagerTheme {
                AppNavigation()
            }
        }
    }
}