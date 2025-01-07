package alexandrade.photos_sync.ui.cloud_wizards

import alexandrade.photos_sync.database.entities.Remote
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import alexandrade.photos_sync.ui.TopAppBarWithBack
import alexandrade.photos_sync.view_models.RemotesViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map


@Composable
fun CloudAccounts(
    onRemoteClick: (Remote) -> Unit,
    onBackClick: () -> Unit,
    onAddCloud: () -> Unit
) {
    val remotesViewModel = viewModel<RemotesViewModel>()

    val remotes by remotesViewModel.uiState.map { state ->
        when (state) {
            is RemotesViewModel.UiState.Success -> state.remotes
            else -> emptyList()
        }
    }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBarWithBack("Contas", onBackClick = onBackClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCloud) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add cloud account"
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            remotes.forEach { remote ->
                ListItem(
                    headlineContent = { Text(remote.name) },
                    supportingContent = { Text(remote.provider.displayName) },
                    leadingContent = {
                        Image(
                            painter = painterResource(id = remote.provider.logoId),
                            contentDescription = "cloud provider logo"
                        )
                    }, modifier = Modifier.clickable(onClick = {
                        onRemoteClick(remote)
                    })
                )
            }
        }
    }
}

@Preview
@Composable
fun CloudSelectionPreview() {
    CloudAccounts(onRemoteClick = {}, onBackClick = {}, onAddCloud = {})
}