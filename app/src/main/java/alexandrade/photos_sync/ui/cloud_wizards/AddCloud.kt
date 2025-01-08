package alexandrade.photos_sync.ui.cloud_wizards

import alexandrade.photos_sync.database.entities.CloudProviders
import alexandrade.photos_sync.ui.TopAppBarWithBack
import alexandrade.photos_sync.view_models.AddCloudViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map

private enum class Stage {
    SELECTION, FORM
}

@Composable
fun AddCloud(onExitClick: () -> Unit) {
    var stage = remember { mutableStateOf(Stage.SELECTION) }
    var buttonEnable = remember { mutableStateOf(false) }
    var buttonText = remember { mutableStateOf("Seguinte") }
    var selectedCloudProvider: CloudProviders? = null

    val viewModel = viewModel<AddCloudViewModel>()

    val remote = viewModel.uiState.map { state ->
        when (state) {
            is AddCloudViewModel.UiState.Error -> null
            AddCloudViewModel.UiState.Loading -> null
            is AddCloudViewModel.UiState.Success -> state.createdRemote
        }
    }.collectAsState(null)

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                "Adicionar conta",
                onBackClick = onExitClick,
                icon = Icons.Filled.Close
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (stage.value) {
                Stage.SELECTION -> CloudProviderSelection(
                    onSelection = { cloudProvider ->
                        selectedCloudProvider = cloudProvider
                        if (buttonEnable.value == false) {
                            buttonEnable.value = true
                        }
                    })

                Stage.FORM -> {
                    when (selectedCloudProvider) {
                        CloudProviders.BACKBLAZE -> BackblazeConfig()
                        null -> Box {  }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1F))
            Button(
                enabled = buttonEnable.value || remote.value != null,
                onClick = {
                    if (stage.value == Stage.SELECTION) {
                        stage.value = Stage.FORM
                        buttonText.value = "Adicionar"
                        buttonEnable.value = false
                    } else if (stage.value == Stage.FORM) {
                        if (remote.value == null) throw Error("Expecting remote at this stage")

                        viewModel.addRemote(remote.value!!)

                        onExitClick()
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) { Text(buttonText.value) }
        }
    }
}

@Composable
private fun CloudProviderSelection(onSelection: (CloudProviders) -> Unit) {
    val selected: MutableState<CloudProviders?> = remember { mutableStateOf(null) }

    CloudProviders.entries.forEach { entry ->
        ListItem(
            headlineContent = { Text(entry.displayName) },
            leadingContent = {
                Image(
                    painter = painterResource(entry.logoId),
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Cloud provider logo"
                )
            },
            trailingContent = {
                RadioButton(
                    selected = selected.value == entry,
                    onClick = { selected.value = entry; onSelection(entry) }
                )
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }

}