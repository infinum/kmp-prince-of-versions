package com.infinum.princeofversions.sample

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

fun setExampleScreenContent(
    activity: ComponentActivity,
    onCheckClick: () -> Unit,
    onCancelTestClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    activity.setContent {
        ExampleScreen(
            modifier = Modifier.fillMaxSize(),
            onCheckClick = { onCheckClick() },
            onCancelTestClick = { onCancelTestClick() },
            onCancelClick = { onCancelClick() },
        )
    }
}
