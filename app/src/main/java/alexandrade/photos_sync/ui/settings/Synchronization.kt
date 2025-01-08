package alexandrade.photos_sync.ui.settings

import alexandrade.photos_sync.ui.TopAppBarWithBack
import alexandrade.photos_sync.R
import alexandrade.photos_sync.view_models.SynchronizationViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map

@Composable
fun Synchronization(onBackClick: () -> Unit, runSyncWorker: () -> Unit) {
    val viewModel = viewModel<SynchronizationViewModel>()

    val syncHistory by viewModel.uiState.map { elem ->
        when (elem) {
            is SynchronizationViewModel.UiState.Error -> listOf()
            SynchronizationViewModel.UiState.Loading -> listOf()
            is SynchronizationViewModel.UiState.Success -> elem.syncHistory
        }
    }.collectAsState(listOf())

    Scaffold(
        topBar = { TopAppBarWithBack("Sincronização", onBackClick = onBackClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = runSyncWorker,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_sync_24),
                        contentDescription = "Add cloud account"
                    )
                },
                text = { Text("Sincronizar agora") }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            syncHistory.forEach { elem ->
                item {
                    ListItem(
                        headlineContent = { Text("Sincronização") },
                        supportingContent = { Text(elem.date.toString()) })
                }
            }
        }
    }
}