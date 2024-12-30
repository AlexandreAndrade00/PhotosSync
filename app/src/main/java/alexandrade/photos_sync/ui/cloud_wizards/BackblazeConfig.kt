package alexandrade.photos_sync.ui.cloud_wizards

import alexandrade.photos_sync.ui.TopAppBarWithBack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BackblazeConfig(onBackClick: () -> Unit) {
    val name = remember { mutableStateOf("") }
    val apiKeyid = remember { mutableStateOf("") }
    val apiKey = remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBarWithBack("Adicionar conta Backblaze", onBackClick = onBackClick) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = name.value,
                label = { Text("Nome") },
                onValueChange = { text: String -> name.value = text })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKeyid.value,
                label = { Text("API Key ID") },
                onValueChange = { text: String -> apiKeyid.value = text })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKey.value,
                label = { Text("API Key") },
                onValueChange = { text: String -> apiKey.value = text })

            FilledTonalButton(onClick = {}) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add backblaze config")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Adicionar")
            }
        }
    }
}

@Preview
@Composable
fun BackblazeConfigPreview() {
    BackblazeConfig(onBackClick = {})
}