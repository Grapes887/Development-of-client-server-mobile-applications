package com.example.module6.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BleDeviceItem(
    val address: String,
    val name: String,
    val rssi: Int,
    val isConnected: Boolean = false
)

data class HeartRateUiState(
    val isBleSupported: Boolean = true,
    val isBluetoothEnabled: Boolean = true,
    val isScanning: Boolean = false,
    val heartRateText: String = "Heart Rate: —",
    val devices: List<BleDeviceItem> = emptyList(),
    val statusMessage: String = "Нажмите «Начать поиск», чтобы найти пульсометр."
)

class HeartRateBleController(
    context: Context
) {
    private val appContext = context.applicationContext

    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scanner: BluetoothLeScanner? = adapter?.bluetoothLeScanner

    private val _uiState = MutableStateFlow(
        HeartRateUiState(
            isBleSupported = adapter != null,
            isBluetoothEnabled = adapter?.isEnabled == true,
            statusMessage = when {
                adapter == null -> "Bluetooth LE недоступен на этом устройстве."
                adapter.isEnabled.not() -> "Bluetooth выключен. Включите его и повторите."
                else -> "Нажмите «Начать поиск», чтобы найти пульсометр."
            }
        )
    )
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    private var currentGatt: BluetoothGatt? = null
    private var connectedAddress: String? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = device.name ?: "Unknown device"
            upsertDevice(
                BleDeviceItem(
                    address = device.address,
                    name = name,
                    rssi = result.rssi,
                    isConnected = device.address == connectedAddress
                )
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                android.bluetooth.BluetoothProfile.STATE_CONNECTED -> {
                    connectedAddress = gatt.device.address
                    _uiState.value = _uiState.value.copy(
                        statusMessage = "Устройство подключено: ${gatt.device.name ?: gatt.device.address}"
                    )
                    markConnected(gatt.device.address, true)
                    gatt.discoverServices()
                }

                android.bluetooth.BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedAddress = null
                    _uiState.value = _uiState.value.copy(
                        heartRateText = "Heart Rate: —",
                        statusMessage = "Соединение закрыто."
                    )
                    markConnected(gatt.device.address, false)
                    currentGatt?.close()
                    currentGatt = null
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(HEART_RATE_SERVICE_UUID) ?: run {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Heart Rate Service не найден."
                )
                return
            }
            val characteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT_UUID) ?: run {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Heart Rate Measurement не найден."
                )
                return
            }
            enableNotifications(gatt, characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_UUID) {
                updateHeartRate(parseHeartRate(value))
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_UUID) {
                updateHeartRate(parseHeartRate(characteristic.value ?: byteArrayOf()))
            }
        }
    }

    fun refreshEnvironmentState() {
        _uiState.value = _uiState.value.copy(
            isBleSupported = adapter != null,
            isBluetoothEnabled = adapter?.isEnabled == true,
            statusMessage = when {
                adapter == null -> "Bluetooth LE недоступен на этом устройстве."
                adapter.isEnabled.not() -> "Bluetooth выключен. Включите его и повторите."
                else -> _uiState.value.statusMessage
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (adapter == null) {
            refreshEnvironmentState()
            return
        }
        if (adapter.isEnabled.not()) {
            refreshEnvironmentState()
            return
        }

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(listOf(filter), settings, scanCallback)
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            statusMessage = "Идёт поиск BLE-устройств с Heart Rate Service..."
        )
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanner?.stopScan(scanCallback)
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            statusMessage = if (connectedAddress == null) {
                "Поиск остановлен."
            } else {
                _uiState.value.statusMessage
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        val device = adapter?.getRemoteDevice(address) ?: return
        stopScan()
        _uiState.value = _uiState.value.copy(
            statusMessage = "Подключение к ${device.name ?: device.address}..."
        )
        currentGatt?.close()
        currentGatt = device.connectGatt(
            appContext,
            false,
            gattCallback,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) BluetoothDevice.TRANSPORT_LE else 0
        )
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        currentGatt?.disconnect()
        currentGatt?.close()
        currentGatt = null
        connectedAddress = null
        _uiState.value = _uiState.value.copy(
            heartRateText = "Heart Rate: —",
            statusMessage = "Соединение закрыто вручную.",
            devices = _uiState.value.devices.map { item -> item.copy(isConnected = false) }
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID) ?: return
        val value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, value)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = value
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
        _uiState.value = _uiState.value.copy(
            statusMessage = "Ожидаем первые уведомления Heart Rate Measurement..."
        )
    }

    private fun parseHeartRate(value: ByteArray): Int {
        if (value.isEmpty()) return 0
        val is16Bit = value[0].toInt() and 0x01 != 0
        return if (is16Bit && value.size >= 3) {
            (value[1].toInt() and 0xFF) or ((value[2].toInt() and 0xFF) shl 8)
        } else if (value.size >= 2) {
            value[1].toInt() and 0xFF
        } else {
            0
        }
    }

    private fun updateHeartRate(value: Int) {
        _uiState.value = _uiState.value.copy(
            heartRateText = if (value > 0) "Heart Rate: $value bpm" else "Heart Rate: —",
            statusMessage = if (value > 0) "Данные получены по уведомлению 0x2A37." else _uiState.value.statusMessage
        )
    }

    private fun upsertDevice(item: BleDeviceItem) {
        val updated = _uiState.value.devices.toMutableList()
        val index = updated.indexOfFirst { device -> device.address == item.address }
        if (index >= 0) {
            updated[index] = item
        } else {
            updated += item
        }
        _uiState.value = _uiState.value.copy(devices = updated.sortedBy { device -> device.name })
    }

    private fun markConnected(address: String, connected: Boolean) {
        _uiState.value = _uiState.value.copy(
            devices = _uiState.value.devices.map { item ->
                if (item.address == address) item.copy(isConnected = connected) else item
            }
        )
    }

    companion object {
        private val HEART_RATE_SERVICE_UUID: UUID =
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        private val HEART_RATE_MEASUREMENT_UUID: UUID =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
