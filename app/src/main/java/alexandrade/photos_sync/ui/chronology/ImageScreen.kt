package alexandrade.photos_sync.ui.chronology

import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.ui.TopAppBarWithBack
import alexandrade.photos_sync.view_models.ImageScreenViewModel
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

@Composable
fun ImageScreen(imageId: UUID, onBackClick: () -> Unit) {
    val viewModel = viewModel<ImageScreenViewModel>()

    viewModel.selectAnImage(imageId)

    val uiState by viewModel.uiState.collectAsState()



    when (uiState) {
        is ImageScreenViewModel.UiState.Loading -> ImageLoading(onBackClick = onBackClick)
        is ImageScreenViewModel.UiState.Success -> {
            val successState = uiState as ImageScreenViewModel.UiState.Success

            if (successState.selectedImage == null) {
                ImageError(onBackClick = onBackClick, message = "Image still exists?")

                return
            }

            ImageLoaded(
                onBackClick = onBackClick,
                images = successState.images,
                initialImage = imageId
            )
        }

        is ImageScreenViewModel.UiState.Error -> ImageError(
            onBackClick = onBackClick,
            message = (uiState as ImageScreenViewModel.UiState.Error).message
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageLoaded(onBackClick: () -> Unit, images: List<Image>, initialImage: UUID) {
    var imageName = remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBarWithBack(title = imageName.value, onBackClick = onBackClick) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        ImagesPager(
            images,
            initialImage,
            Modifier.padding(innerPadding),
            onImageSwipe = { image -> imageName.value = image.name })
    }
}

@Composable
fun ImagesPager(
    images: List<Image>,
    initialImage: UUID,
    modifier: Modifier,
    onImageSwipe: (Image) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { images.size },
        initialPage = images.indexOfFirst { image -> image.uuid == initialImage })

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
    )
    { page ->
        val image = images[page]

        onImageSwipe(image)

        AsyncImage(
            model = image.localPath,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}


@Composable
fun ImageLoading(onBackClick: () -> Unit) {
    Scaffold(topBar = {
        TopAppBarWithBack(
            title = "A carregar imagem",
            onBackClick = onBackClick
        )
    }) { innerPadding ->
        CircularProgressIndicator(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun ImageError(onBackClick: () -> Unit, message: String) {
    Scaffold(topBar = {
        TopAppBarWithBack(
            title = "Erro",
            onBackClick = onBackClick
        )
    }) { innerPadding ->
        Text(
            "Error: $message", modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}