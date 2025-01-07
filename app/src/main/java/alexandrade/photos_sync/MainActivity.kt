package alexandrade.photos_sync

import alexandrade.photos_sync.ui.chronology.ChronologyScreen
import alexandrade.photos_sync.ui.chronology.ImageScreen
import alexandrade.photos_sync.ui.cloud_wizards.AddCloud
import alexandrade.photos_sync.ui.cloud_wizards.BackblazeConfig
import alexandrade.photos_sync.ui.cloud_wizards.CloudAccounts
import alexandrade.photos_sync.ui.settings.Settings
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import alexandrade.photos_sync.ui.theme.PhotosSyncTheme
import alexandrade.photos_sync.utils.*
import alexandrade.photos_sync.workers.SyncWorker
import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class MyApplication : Application() {}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            PhotosSyncTheme {
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute
                ) {
                    composable<HomeRoute> {
                        HomeScreen(
                            navigateToSettings = {
                                navController.navigate(
                                    SettingsRoute
                                )
                            },
                            navigateToImageView = { imageId ->
                                navController.navigate(ImageScreenRoute(imageId = imageId))
                            })
                    }
                    composable<SettingsRoute> {
                        Settings(onBackClick = navController::popBackStack, onAccountsClick = {
                            navController.navigate(
                                CloudAccountsRoute
                            )
                        })
                    }
                    composable<CloudAccountsRoute> {
                        CloudAccounts(
                            onBackClick = navController::popBackStack,
                            onRemoteClick = { remote -> },
                            onAddCloud = { navController.navigate(AddCloudRoute) })
                    }
                    composable<ImageScreenRoute> { backStackEntry ->
                        val route: ImageScreenRoute = backStackEntry.toRoute()

                        ImageScreen(
                            imageId = UUID.fromString(route.imageId),
                            onBackClick = navController::popBackStack
                        )
                    }
                    composable<AddCloudRoute> { AddCloud(onExitClick = navController::popBackStack) }
                }
            }
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()

//        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
//            .setInitialDelay(0, TimeUnit.SECONDS)
//            .setConstraints(constraints)
//            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(application).enqueue(workRequest)

//        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
//            "sync_worker",
//            ExistingPeriodicWorkPolicy.KEEP,
//            workRequest
//        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigateToSettings: () -> Unit, navigateToImageView: (String) -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Photos Sync") }, actions = {
                IconButton(onClick = {
                    navigateToSettings()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings icon"
                    )
                }
            })
        },
        bottomBar = {
            BottomNavigationBar(selectedItem, onChange = { item -> selectedItem = item })
        })
    { innerPadding ->
        when (selectedItem) {
            0 -> ChronologyScreen(innerPadding, navigateToImageView = navigateToImageView)
            else -> {
                Box(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Not implemented")
                }
            }
        }
    }
}

data class BottomBarItem(val label: String, val icon: @Composable () -> Unit)

@Composable
fun BottomNavigationBar(selectedItem: Int, onChange: (Int) -> Unit) {
    val items = listOf(
        BottomBarItem(
            label = "Cronologia",
            icon = {
                Icon(
                    painter = painterResource(R.drawable.gallery_thumbnail),
                    contentDescription = null
                )
            }
        ),
        BottomBarItem(
            label = "Álbuns",
            icon = {
                Icon(
                    painter = painterResource(R.drawable.photo_library),
                    contentDescription = null
                )
            }
        ),
        BottomBarItem(
            label = "Vídeos",
            icon = {
                Icon(
                    painter = painterResource(R.drawable.movie),
                    contentDescription = null,
                )
            }
        ),
    )

    NavigationBar {
        items.forEachIndexed { index, bottomBarItem ->
            NavigationBarItem(
                label = { Text(bottomBarItem.label) },
                selected = selectedItem == index,
                onClick = { onChange(index) },
                icon = { bottomBarItem.icon },
            )
        }
    }
}