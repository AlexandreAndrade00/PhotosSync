package alexandrade.photos_sync.view_models

import alexandrade.photos_sync.cloud_providers.BackblazeB2
import alexandrade.photos_sync.database.entities.Remote
import alexandrade.photos_sync.repository.RemotesRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCloudViewModel(application: Application) : AndroidViewModel(application) {
    private val remotesRepository = RemotesRepository(application)

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val names = remotesRepository.getRemotes().map { elem -> elem.name }

            _uiState.value = UiState.Success(null, names)
        }
    }

    fun createB2Remote(apiKeyId: String, apiKey: String, name: String) {
        val state = _uiState.value

        if (state !is UiState.Success) {
            return
        }

        if (name == "" || apiKeyId == "" || apiKey == "") {
            viewModelScope.launch {
                _uiState.emit(UiState.Error("Incomplete data to create remote"))
            }

            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val remote = BackblazeB2.createBucket(apiKeyId, apiKey, name)

            _uiState.emit(UiState.Success(remote, state.remoteNames))
        }
    }

    fun addRemote(remote: Remote) {
        viewModelScope.launch(Dispatchers.IO) {
            remotesRepository.addRemote(remote)
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val createdRemote: Remote?, val remoteNames: List<String>) : UiState()
        data class Error(val message: String) : UiState()
    }
}