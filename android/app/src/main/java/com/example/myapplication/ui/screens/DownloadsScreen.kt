package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.LinkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@Composable
fun DownloadsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var links by remember { mutableStateOf<List<LinkEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            links = db.linkDao().getAll()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Shared Links", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (links.isEmpty()) {
            Text("No shared links found.", style = MaterialTheme.typography.bodyMedium)
        } else {
            links.forEach { link ->
                Text(
                    text = link.url,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
