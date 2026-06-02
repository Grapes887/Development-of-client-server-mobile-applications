package com.example.module6.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.module6.ble.BleDeviceItem
import com.example.module6.ble.HeartRateUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateMonitorScreen(
    uiState: HeartRateUiState,
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (BleDeviceItem) -> Unit,
    onDisconnect: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Монитор сердечного ритма") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            HeartRateHeader(
                uiState = uiState,
                permissionsGranted = permissionsGranted,
                onRequestPermissions = onRequestPermissions,
                onStartScan = onStartScan,
                onStopScan = onStopScan,
                onDisconnect = onDisconnect
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.devices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Список устройств пока пуст.\nПосле старта сканирования здесь появятся BLE-устройства.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.devices, key = { item -> item.address }) { item ->
                        DeviceCard(
                            item = item,
                            onConnect = { onConnect(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeartRateHeader(
    uiState: HeartRateUiState,
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = uiState.heartRateText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                !uiState.isBleSupported -> {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("BLE недоступен")
                    }
                }

                !permissionsGranted -> {
                    Button(onClick = onRequestPermissions, modifier = Modifier.fillMaxWidth()) {
                        Text("Выдать разрешения BLE")
                    }
                }

                !uiState.isBluetoothEnabled -> {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("Bluetooth выключен")
                    }
                }

                else -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onStartScan,
                            enabled = !uiState.isScanning,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Начать поиск")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = if (uiState.isScanning) onStopScan else onDisconnect,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (uiState.isScanning) "Остановить" else "Отключиться")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    item: BleDeviceItem,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConnect),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.address, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RSSI: ${item.rssi}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (item.isConnected) "Подключено" else "Нажмите для подключения",
                color = if (item.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
