package com.infinum.princeofversions.sample

import PrinceOfVersionsComponents
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.infinum.princeofversions.ConfigurationParser
import com.infinum.princeofversions.Loader
import com.infinum.princeofversions.PrinceOfVersions
import com.infinum.princeofversions.enums.NotificationType
import com.infinum.princeofversions.enums.UpdateStatus
import com.infinum.princeofversions.models.PrinceOfVersionsConfig
import com.infinum.princeofversions.models.UpdateResult
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CustomConfigurationParserExample : ComponentActivity() {

    private lateinit var princeOfVersions: PrinceOfVersions
    private var updateCheckJob: Job? = null

    private val updateUrl = "https://pastebin.com/raw/9CfSVzz4"

    /**
     * Custom parser factory, used for parsing in special format.
     * Custom parser is defined for JSON object containing only one key: minimum_version.
     */
    private class CustomParser : ConfigurationParser<Int> {
        companion object {
            private const val MINIMUM_VERSION = "minimum_version"
        }

        override fun parse(value: String): PrinceOfVersionsConfig<Int> {
            val mandatoryVersion = JSONObject(value).getInt(MINIMUM_VERSION)
            return PrinceOfVersionsConfig(
                mandatoryVersion = mandatoryVersion,
                optionalVersion = null,
                optionalNotificationType = NotificationType.ALWAYS,
                metadata = emptyMap(),
                requirements = emptyMap()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        princeOfVersions = PrinceOfVersions(
            princeOfVersionsComponents = PrinceOfVersionsComponents
                .Builder(this)
                .withConfigurationParser(CustomParser())
                .build()
        )

        setContent {
            CustomParserScreen(
                onCheckClick = { checkForUpdates(isSlow = false) },
                onCancelTestClick = { checkForUpdates(isSlow = true) },
                onCancelClick = ::cancelUpdateCheck,
            )
        }
    }

    override fun onStop() {
        super.onStop()
        cancelUpdateCheck()
    }

    private fun checkForUpdates(isSlow: Boolean) {
        updateCheckJob?.cancel()

        updateCheckJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (isSlow) {
                    delay(DELAY_TIME)
                }
                val loader = Loader { URL(updateUrl).readText() }
                val result = princeOfVersions.checkForUpdates(loader)

                withContext(Dispatchers.Main) {
                    handleUpdateResult(result)
                }
            } catch (e: CancellationException) {
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main) {
                        showToast(getString(R.string.update_check_cancelled))
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(NonCancellable) {
                    withContext(Dispatchers.Main) {
                        showToast(getString(R.string.update_exception, e.message))
                    }
                }
            }
        }
    }

    private fun cancelUpdateCheck() {
        updateCheckJob?.cancel()
    }

    private fun handleUpdateResult(result: UpdateResult<Int>) {
        val message = when (result.status) {
            UpdateStatus.MANDATORY -> getString(
                R.string.update_available_msg,
                getString(R.string.mandatory),
                result.version
            )
            UpdateStatus.OPTIONAL -> getString(
                R.string.update_available_msg,
                getString(R.string.not_mandatory),
                result.version
            )
            UpdateStatus.NO_UPDATE -> getString(R.string.no_update_available)
        }
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val DELAY_TIME = 5000L
    }
}

@Composable
private fun CustomParserScreen(
    onCheckClick: () -> Unit,
    onCancelTestClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onCheckClick) {
            Text(text = stringResource(id = R.string.btn_new_update))
        }

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Button(onClick = onCancelTestClick) {
                Text(text = stringResource(id = R.string.btn_cancel_test))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onCancelClick) {
                Text(text = stringResource(id = R.string.btn_cancel))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomParserScreenPreview() {
    CustomParserScreen({}, {}, {})
}
