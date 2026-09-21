package com.infinum.princeofversions.sample

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            App(
                onCommonUsageClick = ::onCommonUsageClick,
                onCustomParserClick = ::onCustomParserClick,
                onStreamLoaderClick = ::onStreamLoaderClick,
                onCustomCheckerClick = ::onCustomCheckerClick,
                onCustomStorageClick = ::onCustomStorageClick,
                onCustomVersionLogicClick = ::onCustomVersionLogicClick,
            )
        }
    }

    private fun onCommonUsageClick() {
        startActivity(Intent(this, CommonUsageExample::class.java))
    }

    private fun onCustomParserClick() {
        startActivity(Intent(this, CustomConfigurationParserExample::class.java))
    }

    private fun onStreamLoaderClick() {
        startActivity(Intent(this, StreamLoaderExample::class.java))
    }

    private fun onCustomCheckerClick() {
        startActivity(Intent(this, CustomRequirementCheckerExample::class.java))
    }

    private fun onCustomStorageClick() {
        startActivity(Intent(this, CustomStorageExample::class.java))
    }

    private fun onCustomVersionLogicClick() {
        startActivity(Intent(this, CustomVersionLogicExample::class.java))
    }
}
