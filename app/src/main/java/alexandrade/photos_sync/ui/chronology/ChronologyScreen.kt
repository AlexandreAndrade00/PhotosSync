package alexandrade.photos_sync.ui.chronology

import alexandrade.photos_sync.view_models.ChronologyViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.map


@Composable
fun ChronologyScreen(modifier: PaddingValues, navigateToImageView: (String) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Check API level 33
            // Request READ_MEDIA_IMAGES permission here using rememberLauncherForActivityResult as shown in previous examples
            return@remember mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            // Use alternative approach for API levels below 33 (e.g., access images using older methods if necessary)
            return@remember mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permission for accessing media needed!")
        }
        return // Exit early if permission is not granted
    }

    val viewModel = viewModel<ChronologyViewModel>()
    val imageLoader =
        remember { ImageLoader.Builder(context).build() } // Create ImageLoader outside the loop

    val images by viewModel.uiState.map { state ->
        when (state) {
            is ChronologyViewModel.UiState.Success -> state.images
            else -> emptyList()
        }
    }.collectAsState(initial = emptyList())

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 3),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(modifier),
        content = {
            items(images.size) { index ->
                images[index].localPath?.let { path ->
                    ShowImageFromPath(
                        path,
                        imageLoader = imageLoader,
                        onClick = { navigateToImageView(images[index].uuid.toString()) })
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Image path is null")
                    }
                }
            }
        }
    )
}

@Composable
fun ShowImageFromPath(
    imagePath: Uri,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    AsyncImage(
        model = imagePath,
        imageLoader = imageLoader,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop,
    )
}