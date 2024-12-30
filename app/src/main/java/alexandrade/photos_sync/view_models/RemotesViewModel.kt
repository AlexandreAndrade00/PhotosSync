package alexandrade.photos_sync.view_models

import alexandrade.photos_sync.database.entities.Remote
import alexandrade.photos_sync.repository.RemotesRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemotesViewModel(application: Application) : AndroidViewModel(application) {
    private val remotesRepository = RemotesRepository(application)
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                remotesRepository.getRemotes().collect { remotes ->
                    _uiState.value = UiState.Success(remotes)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load items")
            }
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val remotes: List<Remote>) : UiState()
        data class Error(val message: String) : UiState()
    }
}