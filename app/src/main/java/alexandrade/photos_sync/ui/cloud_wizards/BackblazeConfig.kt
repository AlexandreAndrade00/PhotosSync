package alexandrade.photos_sync.ui.cloud_wizards

import alexandrade.photos_sync.cloud_providers.BackblazeB2
import alexandrade.photos_sync.database.entities.Remote
import alexandrade.photos_sync.view_models.AddCloudViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

@Composable
fun BackblazeConfig() {
    val name = remember { mutableStateOf("") }
    val apiKeyid = remember { mutableStateOf("") }
    val apiKey = remember { mutableStateOf("") }
    val nameErrorText = remember { mutableStateOf("") }

    val viewModel = viewModel<AddCloudViewModel>()

    val bucketId = viewModel.uiState.map { state ->
        when (state) {
            is AddCloudViewModel.UiState.Error -> ""
            AddCloudViewModel.UiState.Loading -> ""
            is AddCloudViewModel.UiState.Success -> state.createdRemote?.bucketId ?: ""
        }
    }.collectAsState("")

    val remotesNames = viewModel.uiState.map { state ->
        when (state) {
            is AddCloudViewModel.UiState.Error -> listOf()
            AddCloudViewModel.UiState.Loading -> listOf()
            is AddCloudViewModel.UiState.Success -> state.remoteNames
        }
    }.collectAsState(listOf())

    val regex = Regex("""^[a-zA-Z0-9-]{6,63}$""")

    fun createBucket() {
        if (name.value == "" || apiKeyid.value == "" || apiKey.value == "") return

        viewModel.createB2Remote(apiKeyid.value, apiKey.value, name.value)
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = name.value,
            label = { Text("Nome") },
            isError = nameErrorText.value.isNotEmpty(),
            onValueChange = { text: String ->
                name.value = text

                if (!text.matches(regex)) {
                    nameErrorText.value =
                        "Mínimo de 6 caracteres e máximo de 63. Só é permitido letras, números e travessões."
                } else if (remotesNames.value.contains(text)) {
                    nameErrorText.value = "Nome já existe"
                } else {
                    nameErrorText.value = ""
                }
            },
            supportingText = {
                if (nameErrorText.value.isNotEmpty()) {
                    Text(nameErrorText.value)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = apiKeyid.value,
            label = { Text("API Key ID") },
            onValueChange = { text: String -> apiKeyid.value = text },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = apiKey.value,
            label = { Text("API Key") },
            onValueChange = { text: String -> apiKey.value = text },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = bucketId.value,
                label = { Text("Bucket ID") },
                enabled = false,
                onValueChange = {},
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { createBucket() },
                enabled = name.value != "" && apiKeyid.value != "" && apiKey.value != "" && nameErrorText.value.isEmpty(),
            ) { Text("Gerar") }
        }
    }
}