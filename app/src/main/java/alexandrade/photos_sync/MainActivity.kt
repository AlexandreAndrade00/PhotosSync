package alexandrade.photos_sync

import alexandrade.photos_sync.ui.chronology.ChronologyScreen
import alexandrade.photos_sync.ui.chronology.ImageScreen
import alexandrade.photos_sync.ui.cloud_wizards.AddCloud
import alexandrade.photos_sync.ui.settings.CloudAccounts
import alexandrade.photos_sync.ui.settings.Settings
import alexandrade.photos_sync.ui.settings.Synchronization
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import alexandrade.photos_sync.ui.theme.PhotosSyncTheme
import alexandrade.photos_sync.utils.*
import alexandrade.photos_sync.workers.SyncWorker
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
                        Settings(onBackClick = navController::popBackStack,
                            onAccountsClick = {
                                navController.navigate(
                                    CloudAccountsRoute
                                )
                            },
                            onSyncClick = { navController.navigate(SynchronizationRoute) })
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
                    composable<SynchronizationRoute> {
                        Synchronization(
                            onBackClick = navController::popBackStack,
                            runSyncWorker = { syncOneTime() }
                        )
                    }
                }
            }
        }

        periodicSync()
    }

    private fun syncOneTime() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(application).enqueue(workRequest)
    }

    private fun periodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(0, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "sync_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigateToSettings: () -> Unit, navigateToImageView: (String) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
    )
    { innerPadding ->
        ChronologyScreen(innerPadding, navigateToImageView = navigateToImageView)
    }
}