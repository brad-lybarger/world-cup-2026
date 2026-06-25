package com.blybarger.worldcup2026

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.blybarger.worldcup2026.ui.SimulationScreen
import com.blybarger.worldcup2026.ui.theme.WorldCup2026Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorldCup2026Theme {
                SimulationScreen()
            }
        }
    }
}
