package com.example.workoutlevelup

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.workoutlevelup.ui.navigation.AppNavigation
import com.example.workoutlevelup.ui.theme.DeepNavy
import com.example.workoutlevelup.ui.theme.WorkoutLevelUpTheme
import com.example.workoutlevelup.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val viewModel = ViewModelProvider(this)[WorkoutViewModel::class.java]

        setContent {
            WorkoutLevelUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = DeepNavy
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
