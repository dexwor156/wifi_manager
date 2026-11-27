package com.homewifimanager.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homewifimanager.app.data.AppPreferences
import com.homewifimanager.app.ui.screens.DailyAllowanceScreen
import com.homewifimanager.app.ui.screens.HomeNetworkScreen
import com.homewifimanager.app.ui.screens.PermissionsScreen
import com.homewifimanager.app.ui.screens.WelcomeScreen
import kotlinx.coroutines.launch

object Routes {
    const val WELCOME = "welcome"
    const val HOME_NETWORK = "home_network"
    const val DAILY_ALLOWANCE = "daily_allowance"
    const val PERMISSIONS = "permissions"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val context = LocalContext.current

    val appPreferences = remember {
        AppPreferences(context.applicationContext)
    }

    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {

        /*
         * WELCOME
         */
        composable(Routes.WELCOME) {

            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(
                        Routes.HOME_NETWORK
                    )
                }
            )
        }

        /*
         * HOME NETWORK
         */
        composable(Routes.HOME_NETWORK) {

            HomeNetworkScreen(

                onUseNetwork = { networkName ->

                    coroutineScope.launch {

                        // Save selected Home Wi-Fi.
                        appPreferences.setHomeNetwork(
                            networkName
                        )

                        /*
                         * The network was detected and selected
                         * while the app was in the foreground,
                         * so it is currently validated as the
                         * Home Wi-Fi.
                         */
                        appPreferences
                            .setConnectedToHomeNetwork(
                                true
                            )

                        navController.navigate(
                            Routes.DAILY_ALLOWANCE
                        )
                    }
                },

                onChooseAnotherNetwork = {
                    // Manual network selection will be added later.
                }
            )
        }

        /*
         * DAILY ALLOWANCE
         */
        composable(Routes.DAILY_ALLOWANCE) {

            DailyAllowanceScreen(

                onContinue = { allowanceGb ->

                    coroutineScope.launch {

                        /*
                         * Convert the allowance from GB
                         * to bytes before storing it.
                         */
                        val allowanceBytes =
                            (
                                    allowanceGb *
                                            1024.0 *
                                            1024.0 *
                                            1024.0
                                    )
                                .toLong()

                        appPreferences
                            .setDailyAllowanceBytes(
                                allowanceBytes
                            )

                        navController.navigate(
                            Routes.PERMISSIONS
                        )
                    }
                }
            )
        }

        /*
         * PERMISSIONS
         */
        composable(Routes.PERMISSIONS) {

            PermissionsScreen(

                onSetupPermissions = {
                    /*
                     * Keep this here for now.
                     *
                     * UsageMonitorService is already started
                     * from PermissionsScreen.
                     *
                     * Dashboard navigation will be added once
                     * the Dashboard screen is implemented.
                     */
                }
            )
        }
    }
}