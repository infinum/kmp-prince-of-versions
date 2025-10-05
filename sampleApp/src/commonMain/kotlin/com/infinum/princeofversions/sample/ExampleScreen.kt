package com.infinum.princeofversions.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExampleScreen(
    modifier: Modifier = Modifier,
    onCheckClick: () -> Unit,
    onCancelTestClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(onClick = onCheckClick) {
            Text(text = "Is there any new update?")
        }

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Button(onClick = onCancelTestClick) {
                Text(text = "Test cancel (delayed update check)")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onCancelClick) {
                Text(text = "Cancel")
            }
        }
    }
}
