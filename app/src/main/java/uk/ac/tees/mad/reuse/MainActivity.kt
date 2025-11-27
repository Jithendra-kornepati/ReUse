package uk.ac.tees.mad.reuse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.ac.tees.mad.reuse.data.local.ReuseIdea
import uk.ac.tees.mad.reuse.presentation.HomeScreen
import uk.ac.tees.mad.reuse.presentation.auth.AuthScreen
import uk.ac.tees.mad.reuse.presentation.auth.AuthViewmodel
import uk.ac.tees.mad.reuse.presentation.detail.ReuseDetailScreen
import uk.ac.tees.mad.reuse.presentation.splash.SplashScreen
import uk.ac.tees.mad.reuse.ui.theme.ReUseTheme
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReUseTheme {
                ReUseApp()
            }
        }
    }
}

sealed class Routes(val route: String) {
    object Splash : Routes("splash_screen")
    object Auth : Routes("auth_screen")
    object Home : Routes("home_screen")
    object ReuseDetail : Routes("reuseDetail/{ideaJson}") {
        fun createRoute(ideaJson: String) = "reuseDetail/$ideaJson"
    }
}

@Composable
fun ReUseApp() {
    val navController = rememberNavController()
    val json = Json { ignoreUnknownKeys = true }
    val authViewModel = hiltViewModel<AuthViewmodel>()


    Scaffold(modifier = Modifier.fillMaxSize()) { i ->
        NavHost(navController = navController, startDestination = Routes.Splash.route) {

            composable(Routes.Splash.route) {
                SplashScreen(navController = navController, authViewModel)
            }

            composable(Routes.Auth.route) {
                AuthScreen(navController = navController, authViewModel)
            }

            composable(Routes.Home.route) {
                HomeScreen(navController = navController)
            }

            composable(
                route = Routes.ReuseDetail.route,
                arguments = listOf(navArgument("ideaJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("ideaJson")
                val idea = encoded?.let {
                    val decoded = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                    json.decodeFromString<ReuseIdea>(decoded)
                }
                idea?.let {
                    ReuseDetailScreen(
                        idea = it,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

        }
    }
}
