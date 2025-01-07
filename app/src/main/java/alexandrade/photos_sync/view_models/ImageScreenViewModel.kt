package alexandrade.photos_sync.view_models

import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.repository.MediaRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ImageScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                mediaRepository.getImagesFlow().collect { images ->
                    _uiState.value = UiState.Success(images, null)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load items")
            }
        }
    }

    fun selectAnImage(imageId: UUID) {
        when (_uiState.value) {
            is UiState.Success -> _uiState.value =
                UiState.Success((_uiState.value as UiState.Success).images, imageId)

            is UiState.Error -> return
            UiState.Loading -> return
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val images: List<Image>, val selectedImage: UUID?) : UiState()
        data class Error(val message: String) : UiState()
    }
}