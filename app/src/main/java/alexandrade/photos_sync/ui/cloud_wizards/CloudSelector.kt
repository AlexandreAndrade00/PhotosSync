package alexandrade.photos_sync.ui.cloud_wizards

import alexandrade.photos_sync.database.entities.CloudProviders
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun CloudSelector() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CloudProviders.entries.forEach { entry ->
                ListItem(
                    headlineContent = { Text(entry.displayName) },
                    leadingContent = {
                        Image(
                            painter = painterResource(entry.logoId),
                            contentDescription = "Cloud provider logo"
                        )
                    },
                    modifier = Modifier.clickable(onClick = {

                    }))
            }
        }
    }
}