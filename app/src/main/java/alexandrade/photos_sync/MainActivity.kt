package alexandrade.photos_sync

import alexandrade.photos_sync.ui.chronology.ChronologyScreen
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
import androidx.compose.ui.tooling.preview.Preview
import alexandrade.photos_sync.ui.theme.PhotosSyncTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    var selectedItem by remember { mutableIntStateOf(0) }

    PhotosSyncTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(selectedItem, onChange = { item -> selectedItem = item })
            })
        { innerPadding ->
            when (selectedItem) {
                0 -> ChronologyScreen(innerPadding)
                else -> {
                    Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Not implemented")
                    }
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