package com.apppppp.bluetoothclassictest.screens

import android.os.AsyncTask
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apppppp.bluetoothclassictest.viewmodel.DeviceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.IOException

/**
 * ホーム画面を表示するComposable関数
 *
 * @param navController ナビゲーションを制御するNavHostController
 */
@Composable
fun MainScreen(navController: NavHostController, deviceViewModel: DeviceViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            val connectionStatus by deviceViewModel.connectionStatus.collectAsState()

            LaunchedEffect(key1 = "loadPairedDevices") {
                deviceViewModel.loadPairedDevicesPeriodically(5000)
            }

            DisposableEffect(key1 = "cancelLoading") {
                onDispose {
                    deviceViewModel.cancelPeriodicLoading()
                }
            }

            LaunchedEffect(key1 = Unit) {
                deviceViewModel.signInAnonymously()
            }

            // 2秒ごとに接続状態を確認
            LaunchedEffect(key1 = "monitorConnectionStatus") {
                var lastCheckedTime = System.currentTimeMillis()
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastCheckedTime >= 2000) { // 2秒経過した場合
                        lastCheckedTime = currentTime
                        // 非同期で接続状態を確認
                        deviceViewModel.isDeviceConnected("EC:C9:FF:0F:80:FA").let { isConnected ->
                            if (!isConnected) {
                                deviceViewModel.updateConnectionStatus("接続が切れています")
                            }
                        }
                    }
                    // 非同期でブロックを回避
                    delay(50) // 少し待機してUIを更新できるように
                }
            }

            // 接続が完了するまで再接続を試みる処理
            LaunchedEffect(key1 = "connectUntilSuccess") {
                var lastCheckedTime = System.currentTimeMillis()
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastCheckedTime >= 2000) { // 2秒経過した場合
                        lastCheckedTime = currentTime
                        if (connectionStatus == "接続が完了しました") break
                        // 非同期で接続処理を行う
                        deviceViewModel.onButtonClicked("EC:C9:FF:0F:80:FA")
                    }
                    // 非同期でブロックを回避
                    delay(50) // 少し待機してUIを更新できるように
                }
            }

            // ボタン表示等...
            Text("メインアプリ")
            Button(onClick = {
                navController.navigate("firebase")
            }) {
                Text("Firebaseのデータ")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                navController.navigate("epcReader")
            }) {
                Text("受信データ表示")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                deviceViewModel.onButtonClicked("EC:C9:FF:0F:80:FA")
            }) {
                Text("手動接続")
            }

            if (connectionStatus.isNotEmpty()) {
                Text(text = connectionStatus)
            }
        }
    }
}
