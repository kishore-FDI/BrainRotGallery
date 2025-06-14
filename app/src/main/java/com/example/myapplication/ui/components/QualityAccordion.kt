// package: com.example.myapplication.ui

package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.DropdownMenuWrapper

@Composable
fun QualityAccordion(
    title: String,
    currentQuality: String,
    currentFallback: String,
    onSettingChange: (quality: String, fallback: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val qualities = listOf("1080p", "720p", "480p", "360p")

    val useBest = currentQuality == "BEST"

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text("Quality Preference", style = MaterialTheme.typography.bodyMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = useBest,
                        onClick = {
                            onSettingChange("BEST", currentFallback)
                        }
                    )
                    Text("Best Quality")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !useBest,
                        onClick = {
                            onSettingChange("720p", currentFallback) // default fallback manual
                        }
                    )
                    Text("Select Manually")
                }

                if (!useBest) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Choose Quality:")

                    DropdownMenuWrapper(
                        options = qualities,
                        selected = currentQuality,
                        onSelectedChange = {
                            onSettingChange(it, currentFallback)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fallback Strategy:")

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = currentFallback == "next-higher",
                            onClick = {
                                onSettingChange(currentQuality, "next-higher")
                            }
                        )
                        Text("Next Higher")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = currentFallback == "next-lower",
                            onClick = {
                                onSettingChange(currentQuality, "next-lower")
                            }
                        )
                        Text("Next Lower")
                    }
                }
            }
        }
    }
}
