import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App(
    onCommonUsageClick: () -> Unit = {},
    onCustomParserClick: () -> Unit = {},
    onStreamLoaderClick: () -> Unit = {},
    onCustomCheckerClick: () -> Unit = {},
    onCustomStorageClick: () -> Unit = {},
    onCustomVersionLogicClick: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppButton("Common Usage", onClick = onCommonUsageClick)
            AppButton("Custom Parser", onClick = onCustomParserClick)
            AppButton("Stream Loader", onClick = onStreamLoaderClick)
            AppButton("Custom Checker", onClick = onCustomCheckerClick)
            AppButton("Custom Storage", onClick = onCustomStorageClick)
            AppButton("Custom Version Logic", onClick = onCustomVersionLogicClick)
        }
    }
}

@Composable
private fun AppButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
