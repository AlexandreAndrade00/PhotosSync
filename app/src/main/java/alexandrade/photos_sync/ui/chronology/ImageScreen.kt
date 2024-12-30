package alexandrade.photos_sync.ui.chronology

import alexandrade.photos_sync.view_models.ChronologyViewModel
import alexandrade.photos_sync.view_models.ImageScreenViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImageScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val viewModel = viewModel<ImageScreenViewModel>()

        val uiState by viewModel.uiState.collectAsState()

        when (uiState) {
            is ImageScreenViewModel.UiState.Loading -> {
                CircularProgressIndicator()
            }

            is ImageScreenViewModel.UiState.Success -> {
                val successState = uiState as ChronologyViewModel.UiState.Success

                if (successState.clickedImageIndex == null) {
                    Text("Image still exists?")

                    return@Scaffold
                }

                AsyncImage(
                    model = successState.images[successState.clickedImageIndex].localPath!!,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            is ImageScreenViewModel.UiState.Error -> {
                Text("Error: ${(uiState as ChronologyViewModel.UiState.Error).message}")
            }
        }
    }
}