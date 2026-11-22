package com.first.pnrenq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.first.pnrenq.ui.navigation.AppNavigation
import com.first.pnrenq.ui.theme.PnrenqTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PnrenqTheme {
                AppNavigation()
            }
        }
    }
}