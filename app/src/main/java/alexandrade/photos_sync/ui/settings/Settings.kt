package alexandrade.photos_sync.ui.settings

import alexandrade.photos_sync.ui.TopAppBarWithBack
import alexandrade.photos_sync.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(onAccountsClick: () -> Unit, onBackClick: () -> Unit, onSyncClick: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarWithBack(title = "Definições", onBackClick = onBackClick)
        }
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ListItem(
                headlineContent = { Text("Contas") },
                supportingContent = { Text("Gerir contas da nuvem") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Cloud providers accounts icon"
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    onAccountsClick()
                })
            )
            ListItem(
                headlineContent = { Text("Sincronização") },
                supportingContent = { Text("Histórico sincronização") },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_sync_24),
                        contentDescription = "Sync icon"
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    onSyncClick()
                })
            )
        }
    }
}

@Composable
@Preview
fun SettingsPreview() {
    Settings(onAccountsClick = {}, onBackClick = {}, onSyncClick = {})
}