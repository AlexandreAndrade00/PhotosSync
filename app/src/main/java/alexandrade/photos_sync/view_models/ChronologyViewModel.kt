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


class ChronologyViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                mediaRepository.syncExtenalStorage()

                mediaRepository.getImages().collect { images ->
                    _uiState.value = UiState.Success(images)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load items")
            }
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val images: List<Image>) : UiState()
        data class Error(val message: String) : UiState()
    }
}

