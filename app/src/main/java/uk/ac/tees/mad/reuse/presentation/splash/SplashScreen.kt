package uk.ac.tees.mad.reuse.presentation.splash

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import uk.ac.tees.mad.reuse.R
import uk.ac.tees.mad.reuse.ui.theme.Typography

@Composable
fun SplashScreen(
    //navController: NavController,
    //viewModel: SplashViewModel = viewModel()
) {
    //val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        //viewModel.initialize()
        delay(2500) // Minimum time for splash screen
//        if (uiState.isLoggedIn) {
//            navController.navigate("home") {
//                popUpTo("splash") { inclusive = true }
//            }
//        } else {
//            navController.navigate("auth") {
//                popUpTo("splash") { inclusive = true }
//            }
//        }
    }

    SplashContent(
        ecoFact = "Reusing one glass bottle saves enough energy to power a TV for 3 hours.",
    )
}

@Composable
private fun SplashContent(
    ecoFact: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(id = R.drawable.re_use_icon),
                contentDescription = "ReUse App Icon",
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("ReUse", fontWeight = FontWeight.Bold, fontSize = 24.sp)

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut(),
                visible = true
            ) {
                Text(
                    text = ecoFact,
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
