package com.example.tmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tmap.core.navigation.GameRoute
import com.example.tmap.core.navigation.GlobalLeaderboardRoute
import com.example.tmap.core.navigation.SetupRoute
import com.example.tmap.feature.game.ui.GameScreenRoot
import com.example.tmap.feature.setup.ui.SetupScreenRoot
import com.example.tmap.ui.theme.TMAPTheme
import com.example.tmap.core.navigation.LeaderboardRoute
import com.example.tmap.feature.players.ui.GlobalLeaderboardScreenRoot
import com.example.tmap.feature.players.ui.LeaderboardScreenRoot
import com.example.tmap.core.notifications.TmapNotificationManager
import com.example.tmap.feature.players.repository.FirestoreRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TMAPTheme {

                TMAPTheme {
                    val context = LocalContext.current
                    val notificationManager = remember { TmapNotificationManager(context) }
                    val firestoreRepository = remember { FirestoreRepository() }

                    // Žádost o povolení notifikací
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                        ) { /* todo zamítnutí */ }

                        LaunchedEffect(Unit) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    //naslouchání na pozadí
                    LaunchedEffect(Unit) {
                        firestoreRepository.listenForLeaderboardChanges(notificationManager)
                    }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SetupRoute,
        modifier = modifier
    ) {

        composable<SetupRoute> {
            SetupScreenRoot(
                onNavigateToGame = { score, doubleOut, p1Id, p1Name, p2Id, p2Name ->
                    navController.navigate(GameRoute(score, doubleOut, p1Id, p1Name, p2Id, p2Name))
                },
                onNavigateToLeaderboard = {
                    navController.navigate(LeaderboardRoute)
                },
                onNavigateToGlobalLeaderboard = {
                    navController.navigate(GlobalLeaderboardRoute)
                }
            )
        }

        composable<GlobalLeaderboardRoute> {
            GlobalLeaderboardScreenRoot(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<GameRoute> {
            GameScreenRoot()
        }

        composable<LeaderboardRoute> {
            LeaderboardScreenRoot(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
}