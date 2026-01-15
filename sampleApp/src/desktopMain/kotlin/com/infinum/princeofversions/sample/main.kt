package com.infinum.princeofversions.sample

import App
import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.infinum.princeofversions.Loader
import com.infinum.princeofversions.PrinceOfVersions
import com.infinum.princeofversions.RequirementsNotSatisfiedException
import com.infinum.princeofversions.UpdateResult
import com.infinum.princeofversions.UpdateStatus
import java.net.URI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PrinceOfVersions JVM Sample",
    ) {
        AppNavigationHost()
    }
}

class AppState(
    private val coroutineScope: CoroutineScope,
    private val exampleName: String
) {

    var statusText by mutableStateOf("Click a button to check for updates.")
        private set

    private var updateCheckJob: Job? = null

    private val princeOfVersions: PrinceOfVersions = PrinceOfVersions(mainClass = javaClass) {
        when (exampleName) {
            "Custom Parser" -> {
                withConfigurationParser(JvmCustomParser())
            }
            "Custom Storage" -> {
                withStorage(JvmInMemoryStorage())
            }
            "Custom Version Logic" -> {
                withVersionProvider(JvmHardcodedVersionProvider())
                withVersionComparator(JvmDeveloperBuildVersionComparator())
            }
            "Custom Checker" -> {
                val checkers = mapOf("requiredNumberOfLetters" to JvmExampleRequirementsChecker())
                withRequirementCheckers(checkers)
            }
        }
    }

    fun checkForUpdates(isSlow: Boolean = false) {
        updateCheckJob?.cancel()
        statusText = "Checking for updates..."

        updateCheckJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                if (isSlow) {
                    statusText = "Starting delayed check (4s)..."
                    delay(4000L)
                    statusText = "Checking for updates..."
                }

                val loader = when (exampleName) {
                    "Stream Loader" -> {
                        val stream = javaClass.getResourceAsStream("/update.json")
                            ?: throw IllegalStateException("Resource file not found: /update.json. Make sure it's in src/jvmMain/resources.")
                        Loader { stream.bufferedReader().use { it.readText() } }
                    }
                    else -> {
                        val url = when (exampleName) {
                            "Common Usage", "Custom Storage", "Custom Version Logic" -> "https://pastebin.com/raw/SCyxsrK0"
                            "Custom Parser" -> "https://pastebin.com/raw/9CfSVzz4"
                            "Custom Checker" -> "https://pastebin.com/raw/fdXFhsRE"
                            else -> "https://pastebin.com/raw/VMgd71VH"
                        }
                        Loader { URI(url).toURL().readText() }
                    }
                }

                val result = princeOfVersions.checkForUpdates(loader)
                handleUpdateResult(result)
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    t.printStackTrace()
                    statusText = if(t is RequirementsNotSatisfiedException) {
                        "Requirements not satisfied."
                    } else {
                        "Error: ${t.message}"
                    }
                }
            }
        }
    }

    fun cancelUpdateCheck() {
        if (updateCheckJob?.isActive == true) {
            updateCheckJob?.cancel()
            statusText = "Update check cancelled."
        } else {
            statusText = "Nothing to cancel."
        }
    }

    private fun handleUpdateResult(result: UpdateResult) {
        statusText = when (result.status) {
            UpdateStatus.MANDATORY -> "A mandatory update to version ${result.version} is available."
            UpdateStatus.OPTIONAL -> "An optional update to version ${result.version} is available."
            UpdateStatus.NO_UPDATE -> "You are on the latest version."
        }
    }
}

@Composable
fun ExampleDetailsScreen(exampleName: String, onBackClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val state = remember(exampleName) { AppState(coroutineScope, exampleName) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Running: $exampleName",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(16.dp),
        )
        ExampleScreen(
            onCheckClick = { state.checkForUpdates() },
            onCancelTestClick = { state.checkForUpdates(isSlow = true) },
            onCancelClick = { state.cancelUpdateCheck() },
        )
        Text(
            text = state.statusText,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(16.dp),
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBackClick) {
            Text("Back to Menu")
        }
    }
}

@Composable
@Preview
fun AppNavigationHost() {
    var currentScreen by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        when (currentScreen) {
            null -> {
                App(
                    onCommonUsageClick = { currentScreen = "Common Usage" },
                    onCustomParserClick = { currentScreen = "Custom Parser" },
                    onStreamLoaderClick = { currentScreen = "Stream Loader" },
                    onCustomCheckerClick = { currentScreen = "Custom Checker" },
                    onCustomStorageClick = { currentScreen = "Custom Storage" },
                    onCustomVersionLogicClick = { currentScreen = "Custom Version Logic" },
                    onJavaUsageClick = { currentScreen = "Java Usage" },
                )
            }
            "Java Usage" -> {
                JavaUsageExampleScreen(
                    onBackClick = { currentScreen = null },
                )
            }
            else -> {
                ExampleDetailsScreen(
                    exampleName = currentScreen!!,
                    onBackClick = { currentScreen = null },
                )
            }
        }
    }
}
