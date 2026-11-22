package com.first.pnrenq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.theme.PnrenqTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PnrenqTheme {
                val pnrs = remember { MockPnrData.sample() }
                HomeScreen(
                    pnrs = pnrs,
                    onAddClick = { },
                    onPnrClick = { }
                )
            }
        }
    }
}