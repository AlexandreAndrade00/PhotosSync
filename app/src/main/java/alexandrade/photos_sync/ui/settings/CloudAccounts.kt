package alexandrade.photos_sync.ui.settings

import alexandrade.photos_sync.database.entities.Remote
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import alexandrade.photos_sync.ui.TopAppBarWithBack
import alexandrade.photos_sync.view_models.CloudAccountsViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map
import kotlin.math.exp


@Composable
fun CloudAccounts(
    onRemoteClick: (Remote) -> Unit,
    onBackClick: () -> Unit,
    onAddCloud: () -> Unit
) {
    var selectedRemote: String? = null
    var openAlertDialog by remember { mutableStateOf(false) }
    val cloudAccountsViewModel = viewModel<CloudAccountsViewModel>()

    val remotes by cloudAccountsViewModel.uiState.map { state ->
        when (state) {
            is CloudAccountsViewModel.UiState.Success -> state.remotes
            else -> emptyList()
        }
    }.collectAsState(initial = emptyList())

    val principalRemote = remotes.firstOrNull { remote -> remote.principal }

    if (openAlertDialog) {
        DeleteDialog(
            onDismissRequest = { openAlertDialog = false },
            onConfirmation = {
                openAlertDialog = false

                val lastSelected = selectedRemote

                if (lastSelected != null) {
                    cloudAccountsViewModel.deleteRemote(lastSelected)
                }
            })
    }

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
            if (principalRemote != null) {
                Text(
                    "Conta Principal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 16.dp)
                )
                ListItem(
                    headlineContent = { Text(principalRemote.name) },
                    supportingContent = { Text(principalRemote.provider.displayName) },
                    leadingContent = {
                        Image(
                            painter = painterResource(id = principalRemote.provider.logoId),
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Crop,
                            contentDescription = "cloud provider logo"
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                "Todas as contas", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 16.dp)
            )
            remotes.forEach { remote ->
                var expanded by remember { mutableStateOf(false) }


                ListItem(
                    headlineContent = { Text(remote.name) },
                    supportingContent = { Text(remote.provider.displayName) },
                    leadingContent = {
                        Image(
                            painter = painterResource(id = remote.provider.logoId),
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Crop,
                            contentDescription = "cloud provider logo"
                        )
                    },
                    trailingContent = {
                        Box {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "more options"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = !expanded }) {
                                DropdownMenuItem(
                                    text = { Text("Definir como principal") },
                                    onClick = {
                                        cloudAccountsViewModel.setPrincipalRemote(remote.name)
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        selectedRemote = remote.name
                                        openAlertDialog = true
                                        expanded = false
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.clickable(onClick = {
                        onRemoteClick(remote)
                    })
                )
            }
        }
    }
}

@Composable
private fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Delete, contentDescription = "delete Icon")
        },
        title = {
            Text(text = "Apagar conta?")
        },
        text = {
            Text(
                text = "De certeza que pretende apagar a conta? A sincronização será desativada. " +
                        "As fotos já sincronizadas não serão apagadas."
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            Button(
                onClick = onConfirmation
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancelar")
            }
        }
    )
}