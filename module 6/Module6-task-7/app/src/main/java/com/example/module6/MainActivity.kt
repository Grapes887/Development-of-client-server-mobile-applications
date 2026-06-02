package com.example.module6

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.module6.ble.HeartRateBleController
import com.example.module6.presentation.ui.screen.HeartRateMonitorScreen
import com.example.module6.presentation.viewmodel.HeartRateViewModel
import com.example.module6.ui.theme.Module6Theme

class MainActivity : ComponentActivity() {

    private val viewModel: HeartRateViewModel by viewModels {
        HeartRateViewModel.Factory(
            controller = HeartRateBleController(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module6Theme {
                BleTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    var permissionsGranted by remember { mutableStateOf(false) }
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { result ->
                        permissionsGranted = result.values.all { granted -> granted }
                        if (permissionsGranted) {
                            viewModel.onPermissionsGranted()
                        }
                    }

                    LaunchedEffect(Unit) {
                        launcher.launch(requiredPermissions())
                    }

                    HeartRateMonitorScreen(
                        uiState = uiState,
                        permissionsGranted = permissionsGranted,
                        onRequestPermissions = { launcher.launch(requiredPermissions()) },
                        onStartScan = viewModel::startScan,
                        onStopScan = viewModel::stopScan,
                        onConnect = viewModel::connectToDevice,
                        onDisconnect = viewModel::disconnect
                    )
                }
            }
        }
    }
}

private fun requiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

@Composable
private fun BleTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
