package com.infinum.princeofversions.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun JavaUsageExampleScreen(onBackClick: () -> Unit) {
    var statusText by remember { mutableStateOf("Click a button to check for updates.") }
    val javaExample = remember {
        JavaUsageExample { newStatus ->
            statusText = newStatus
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Running: Java Usage",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp),
        )
        ExampleScreen(
            onCheckClick = { javaExample.checkForUpdates() },
            onCancelTestClick = { javaExample.checkForUpdatesWithDelay() },
            onCancelClick = { javaExample.cancelUpdateCheck() },
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(16.dp),
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBackClick) {
            Text("Back to Menu")
        }
    }
}
