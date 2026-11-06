package com.example.orbitlane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.orbitlane.ui.navigation.OrbitNavigation
import com.example.orbitlane.ui.theme.OrbitLaneTheme
import com.example.orbitlane.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by viewModel.themePreference.collectAsState()

            OrbitLaneTheme(themePreference = themePreference) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OrbitNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
