package com.apppppp.bluetoothclassictest.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apppppp.bluetoothclassictest.model.BTClientManager
import com.apppppp.bluetoothclassictest.model.BTDeviceInfo
import com.apppppp.bluetoothclassictest.model.BTRecieveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.sql.Timestamp
import com.google.firebase.database.*


class DeviceViewModel : ViewModel() {

    private var _btClientManager: BTClientManager = BTClientManager()
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val _devicesInfo = MutableStateFlow<List<BTDeviceInfo>>(emptyList())
    val devicesInfo: StateFlow<List<BTDeviceInfo>> = _devicesInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _receivedDataList = MutableStateFlow<List<BTRecieveData>>(emptyList())
    val receivedDataList: StateFlow<List<BTRecieveData>> = _receivedDataList.asStateFlow()

    // 最新のデータを保持するState
    private val _tbData = MutableStateFlow<String?>(null)//TB用データ
    val tbData: StateFlow<String?> = _tbData.asStateFlow()

    private val _speed = MutableStateFlow<String?>(null)//機速予定
    val speed: StateFlow<String?> = _speed.asStateFlow()

    private val _eAngle = MutableStateFlow<String?>(null)//E角度予定
    val eAngle: StateFlow<String?> = _eAngle.asStateFlow()

    private val _rAngle = MutableStateFlow<String?>(null)//R角度予定
    val rAngle: StateFlow<String?> = _rAngle.asStateFlow()


    private val _mainData = MutableStateFlow<String?>(null)//メイン基板用データ
    val mainData: StateFlow<String?> = _mainData.asStateFlow()

    private val _height = MutableStateFlow<String?>(null)//高度予定
    val height: StateFlow<String?> = _height.asStateFlow()

    private val _rpm = MutableStateFlow<String?>(null)//回転数予定
    val rpm: StateFlow<String?> = _rpm.asStateFlow()


    private val _atiData = MutableStateFlow<String?>(null)//姿勢角データ
    val atiData: StateFlow<String?> = _atiData.asStateFlow()

    private val _roll = MutableStateFlow<String?>(null)//ロール予定
    val roll: StateFlow<String?> = _roll.asStateFlow()

    private val _pitch = MutableStateFlow<String?>(null)//ピッチ予定
    val pitch: StateFlow<String?> = _pitch.asStateFlow()

    private val _yaw = MutableStateFlow<String?>(null)//ヨー予定
    val yaw: StateFlow<String?> = _yaw.asStateFlow()


    private val _swData = MutableStateFlow<String?>(null)//スイッチデータ
    val swData: StateFlow<String?> = _swData.asStateFlow()

    private val _sw1 = MutableStateFlow<String?>(null)
    val sw1: StateFlow<String?> = _sw1.asStateFlow()

    private val _sw2 = MutableStateFlow<String?>(null)
    val sw2: StateFlow<String?> = _sw2.asStateFlow()

    private val _sw3 = MutableStateFlow<String?>(null)
    val sw3: StateFlow<String?> = _sw3.asStateFlow()

    /*
    *     int型の書き方の例
    *     private val _currentY = MutableStateFlow<Int>(0)
    *     val currentY: StateFlow<Int> = _currentY.asStateFlow()
    */

    private var periodicLoadJob: Job? = null

    // 接続状態を管理するStateFlow
    private val _connectionStatus = MutableStateFlow<String>("")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    fun loadPairedDevicesPeriodically(repeatInterval: Long) {
        periodicLoadJob = viewModelScope.launch {
            while (isActive) {
                Log.d("mopi", "loadPairedDevicesPeriodically")
                loadPairedDevices()
                delay(repeatInterval)
            }
        }
    }

    fun cancelPeriodicLoading() {
        periodicLoadJob?.cancel()
    }

    private fun loadPairedDevices() {
        viewModelScope.launch {
            _devicesInfo.value = pairedDevicesInfo
        }
    }

    val pairedDevicesInfo: List<BTDeviceInfo>
        @SuppressLint("MissingPermission")
        get() = bluetoothAdapter?.bondedDevices?.map { device ->
            val isConnected = _btClientManager.isConnected(device.address)
            Log.d("mopi", "device: ${device.address}")
            Log.d("mopi", "isConnected: $isConnected")

            BTDeviceInfo(
                name = device.name ?: "N/A",
                address = device.address,
                isPaired = true,
                isConnected = isConnected
            )
        } ?: emptyList()

    fun onDeviceClicked(index: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val deviceAddress = _devicesInfo.value[index].address

            withContext(Dispatchers.IO) {
                try {
                    _btClientManager.connectToDevice(deviceAddress)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }

            _isLoading.value = false

            val isConnected = _btClientManager.isConnected(deviceAddress)
            if (isConnected) {
                startReceivingData(deviceAddress)
            }

            val currentDevices = _devicesInfo.value.toMutableList()
            val updatedDevice = currentDevices[index].copy(isConnected = isConnected)
            currentDevices[index] = updatedDevice
            _devicesInfo.value = currentDevices
        }
    }

    fun onButtonClicked(deviceAddress: String) {
        viewModelScope.launch {
            // 接続状態の初期化
            _connectionStatus.value = "接続中..."
            Log.d("DeviceViewModel","接続中")

            // IOスレッドで接続処理を実行
            withContext(Dispatchers.IO) {
                try {
                    _btClientManager.connectToDevice(deviceAddress)
                } catch (e: IOException) {
                    e.printStackTrace()
                    _connectionStatus.value = "接続に失敗しました: ${e.message}"
                    return@withContext
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    _connectionStatus.value = "接続に失敗しました: ${e.message}"
                    return@withContext
                }
            }

            // ロード完了後の処理
            _isLoading.value = false

            // 接続状態の確認
            val isConnected = _btClientManager.isConnected(deviceAddress)
            if (isConnected) {
                _connectionStatus.value = "接続が完了しました"
                Log.d("DeviceViewModel","接続完了")
                startReceivingData(deviceAddress)
            } else {
                _connectionStatus.value = "接続できませんでした"
                Log.d("DeviceViewModel","接続失敗")
            }

            // 接続状態をデバイスリストに反映
            val currentDevices = _devicesInfo.value.toMutableList()

            // deviceAddressでデバイスを検索して、接続状態を更新
            val deviceIndex = currentDevices.indexOfFirst { it.address == deviceAddress }

            // デバイスがリストに存在する場合
            if (deviceIndex != -1) {
                val updatedDevice = currentDevices[deviceIndex].copy(isConnected = isConnected)
                currentDevices[deviceIndex] = updatedDevice
                _devicesInfo.value = currentDevices
            }
        }
    }



    private fun startReceivingData(deviceAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            while (_btClientManager.isConnected(deviceAddress)) {
                try {
                    val rawData = _btClientManager.receiveDataFromDevice(deviceAddress)
                    val deviceName: String? = _btClientManager.deviceName(deviceAddress)

                    if (rawData != null) {
                        val receiveData = BTRecieveData(
                            deviceName = deviceName,
                            deviceAddress = deviceAddress,
                            data = rawData,  // カテゴライズせずにそのまま保存
                            timestamp = Timestamp(System.currentTimeMillis())
                        )
                        withContext(Dispatchers.Main) {
                            _receivedDataList.value += listOf(receiveData)
                            updateLatestData(rawData)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    private fun updateLatestData(rawData: String) {
        // データをカンマ（,）で分割
        val dataParts = rawData.split(",")

        for (part in dataParts) {
            val keyValue = part.split(":")
            if (keyValue.size == 2) {
                val key = keyValue[0].trim()
                val value = keyValue[1].trim()

                when (key) {
                    // TBデータ
                    "A" -> _speed.value = value
                    "B" -> _eAngle.value = value
                    "C" -> _rAngle.value = value

                    // メイン基板データ
                    "H" -> _height.value = value
                    "I" -> _rpm.value = value

                    // 姿勢角データ
                    "O" -> _roll.value = value
                    "P" -> _pitch.value = value
                    "Q" -> _yaw.value = value

                    // スイッチデータ
                    "V" -> _sw1.value = value
                    "W" -> _sw2.value = value
                    "X" -> _sw3.value = value
                }
            }
        }

        // 受信データを保持
        _tbData.value = rawData.trim()
        _mainData.value = rawData.trim()
        _atiData.value = rawData.trim()
        _swData.value = rawData.trim()
    }


    //接続状態を監視する関数
    // 接続状態をチェックする関数
    fun isDeviceConnected(deviceAddress: String): Boolean {
        return _btClientManager.isConnected(deviceAddress)
    }

    // 接続状態を更新する関数
    fun updateConnectionStatus(status: String) {
        _connectionStatus.value = status
    }


    // Firebase匿名サインイン
    fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    Log.d("FirebaseAuth", "Signed in anonymously: ${user?.uid}")
                } else {
                    Log.e("FirebaseAuth", "Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("data")

    private val _firebaseData = MutableStateFlow("データなし")
    val firebaseData: StateFlow<String> = _firebaseData

    init {
        startFirebaseListener()
    }

    private fun startFirebaseListener() {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newData = snapshot.getValue(String::class.java)
                if (newData != null) {
                    viewModelScope.launch {
                        _firebaseData.value = newData
                    }
                    Log.d("DeviceViewModel", "Received data: $newData")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DeviceViewModel", "Failed to read value.", error.toException())
            }
        })
    }

}

