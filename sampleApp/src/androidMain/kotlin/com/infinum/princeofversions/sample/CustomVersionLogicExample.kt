package com.infinum.princeofversions.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.infinum.princeofversions.ApplicationVersionProvider
import com.infinum.princeofversions.Loader
import com.infinum.princeofversions.PrinceOfVersions
import com.infinum.princeofversions.UpdateResult
import com.infinum.princeofversions.UpdateStatus
import com.infinum.princeofversions.VersionComparator
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

class CustomVersionLogicExample : ComponentActivity() {

    private lateinit var princeOfVersions: PrinceOfVersions
    private var updateCheckJob: Job? = null

    // Using a URL with a standard integer-based configuration
    private val updateUrl = "https://pastebin.com/raw/KPzkwNuP"

    /**
     * A custom version provider that returns a hardcoded integer version.
     * In a real app, this could come from a local file, database, or build flavor config.
     */
    class HardcodedVersionProvider : ApplicationVersionProvider {
        private val currentAppVersion = 26L
        override fun getVersion(): Long {
            return currentAppVersion
        }
    }

    /**
     * A custom comparator with a special rule for developer builds.
     */
    class DeveloperBuildVersionComparator : VersionComparator {
        /**
         * Compares versions, but treats any remote version ending in '0' as a
         * developer build that should not trigger an update.
         */
        override fun compare(firstVersion: Long, secondVersion: Long): Int {
            // Custom rule: never show an update for developer builds (versions ending in 0)
            if (secondVersion % 10 == 0L) {
                return -1 // Treat as "no update available"
            }
            return secondVersion.compareTo(firstVersion)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        princeOfVersions = PrinceOfVersions(context = this) {
            withVersionProvider(HardcodedVersionProvider())
            withVersionComparator(DeveloperBuildVersionComparator())
        }

        setContent {
            ExampleScreen(
                modifier = Modifier.fillMaxSize(),
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

    private fun handleUpdateResult(result: UpdateResult) {
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
        private const val DELAY_TIME = 10000L
    }
}

@Preview
@Composable
private fun CustomVersionLogicScreenPreview() {
    ExampleScreen(onCheckClick = {}, onCancelClick = {}, onCancelTestClick = {})
}

