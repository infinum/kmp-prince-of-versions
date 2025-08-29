package com.infinum.princeofversions.sample

import App
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            )
        }
    }

    private fun onCommonUsageClick() {
        startActivity(Intent(this, CommonUsageExample::class.java))
    }

    private fun onCustomParserClick() {
        Toast.makeText(this, "Functionality not yet implemented", Toast.LENGTH_SHORT).show()
    }

    private fun onStreamLoaderClick() {
        startActivity(Intent(this, StreamLoaderExample::class.java))
    }

    private fun onCustomCheckerClick() {
        startActivity(Intent(this, CustomRequirementCheckerExample::class.java))
    }
}
