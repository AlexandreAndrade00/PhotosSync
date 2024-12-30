package alexandrade.photos_sync

import alexandrade.photos_sync.ui.chronology.ChronologyScreen
import alexandrade.photos_sync.ui.chronology.ImageScreen
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

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
                        HomeScreen(navigateToSettings = {
                            navController.navigate(
                                SettingsRoute
                            )
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
                            onRemoteClick = { remote -> navController.navigate(remote.provider.route) })
                    }
                    composable<BackblazeConfigRoute> { BackblazeConfig(onBackClick = navController::popBackStack) }
                    composable<ImageScreenRoute> { ImageScreen() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigateToSettings: () -> Unit) {
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
            0 -> ChronologyScreen(innerPadding)
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