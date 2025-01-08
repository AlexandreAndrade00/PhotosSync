package alexandrade.photos_sync.view_models

import alexandrade.photos_sync.database.entities.SyncHistory
import alexandrade.photos_sync.repository.MediaRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SynchronizationViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = MediaRepository(application)
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mediaRepository.getSyncHistoryRemote().collect { syncHistory ->
                _uiState.value = UiState.Success(syncHistory)
            }
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val syncHistory: List<SyncHistory>) : UiState()
        data class Error(val message: String) : UiState()
    }
}