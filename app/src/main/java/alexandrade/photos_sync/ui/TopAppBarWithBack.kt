package alexandrade.photos_sync.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBack(
    title: String,
    onBackClick: () -> Unit,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {
                onBackClick()
            }) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Back button"
                )
            }
        })
}