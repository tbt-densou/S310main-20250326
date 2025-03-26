package com.apppppp.bluetoothclassictest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apppppp.bluetoothclassictest.screens.SensorData
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.tasks.await

class FirebaseViewModel : ViewModel() {
    private val databaseRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("sensorData")

    private val _latestData = MutableStateFlow<SensorData?>(null)
    val latestData: StateFlow<SensorData?> = _latestData.asStateFlow()

    init {
        startPollingFirebase()
    }

    // Firebaseをポーリングしてデータを取得
    private fun startPollingFirebase() {
        viewModelScope.launch {
            while (true) {
                try {
                    fetchLatestData()  // Firebaseから最新データを取得
                    delay(100)  // 100msごとにデータを取得
                } catch (e: Exception) {
                    Log.e("FirebaseViewModel", "Error polling Firebase: ${e.message}")
                }
            }
        }
    }

    // Firebaseから最新データを取得する
    private suspend fun fetchLatestData() {
        try {
            val snapshot = databaseRef.orderByChild("timestamp").limitToLast(1).get().await()

            if (snapshot.exists()) {
                // FirebaseからのデータをMapとして取得
                val dataMap = snapshot.children.firstOrNull()?.value as? Map<String, Any>

                if (dataMap != null) {
                    // MapをSensorDataに変換
                    val sensorData = SensorData(
                        sw1 = (dataMap["sw1"] as? String)?.toDouble(),
                        speed = (dataMap["speed"] as? String)?.toDouble(),
                        height = (dataMap["height"] as? String)?.toDouble(),
                        rpm = (dataMap["rpm"] as? String)?.toDouble(),
                        roll = (dataMap["roll"] as? String)?.toDouble(),
                        pitch = (dataMap["pitch"] as? String)?.toDouble(),
                        yaw = (dataMap["yaw"] as? String)?.toDouble(),
                        eAngle = (dataMap["eAngle"] as? String)?.toDouble(),
                        rAngle = (dataMap["rAngle"] as? String)?.toDouble(),
                        timestamp = dataMap["timestamp"] as? Long
                    )
                    Log.d("FirebaseViewModel", "Fetched data: $sensorData")
                    _latestData.value = sensorData
                } else {
                    Log.e("FirebaseViewModel", "Invalid data received from Firebase.")
                }
            } else {
                Log.e("FirebaseViewModel", "No data found in Firebase.")
            }
        } catch (e: Exception) {
            Log.e("FirebaseViewModel", "Error fetching data from Firebase: ${e.message}")
        }
    }

}
