package com.example.module6.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.module6.ble.BleDeviceItem
import com.example.module6.ble.HeartRateBleController
import com.example.module6.ble.HeartRateUiState
import kotlinx.coroutines.flow.StateFlow

class HeartRateViewModel(
    private val controller: HeartRateBleController
) : ViewModel() {

    val uiState: StateFlow<HeartRateUiState> = controller.uiState

    fun onPermissionsGranted() {
        controller.refreshEnvironmentState()
    }

    fun startScan() {
        controller.startScan()
    }

    fun stopScan() {
        controller.stopScan()
    }

    fun connectToDevice(device: BleDeviceItem) {
        controller.connect(device.address)
    }

    fun disconnect() {
        controller.disconnect()
    }

    class Factory(
        private val controller: HeartRateBleController
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HeartRateViewModel(controller) as T
        }
    }
}
