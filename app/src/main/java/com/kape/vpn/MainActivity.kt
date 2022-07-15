package com.kape.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kape.connection.ui.ConnectionScreen
import com.kape.login.ui.LoginScreen
import com.kape.login.ui.LoginWithEmailScreen
import com.kape.region_selection.ui.RegionSelectionScreen
import com.kape.router.*
import com.kape.splash.ui.SplashScreen
import com.kape.uicomponents.theme.PIATheme
import com.kape.vpn_permissions.ui.VpnSystemProfileScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val router: Router by inject()
    private val destinationsForClearBackStack = listOf(Splash.Main, Login.Main, VpnPermission.Main, Connection.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            lifecycleScope.launchWhenCreated {
                router.navigation.collect {
                    if (it.isNotBlank()) {
                        if (it == NavigateBack) {
                            navController.navigateUp()
                        } else {
                            navController.navigate(it) {
                                launchSingleTop = true
                                if (it in destinationsForClearBackStack) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }

            PIATheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = Splash.Main) {
                        composable(Login.Main) { LoginScreen(navController = navController) }
                        composable(Login.WithEmail) { LoginWithEmailScreen(navController = navController) }
                        composable(VpnPermission.Main) { VpnSystemProfileScreen() }
                        composable(Splash.Main) { SplashScreen() }
                        composable(Connection.Main) { ConnectionScreen() }
                        composable(RegionSelection.Main) { RegionSelectionScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PIATheme {
        Greeting("Android")
    }
}