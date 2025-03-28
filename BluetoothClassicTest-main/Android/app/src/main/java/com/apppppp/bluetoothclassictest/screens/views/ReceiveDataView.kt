package com.apppppp.bluetoothclassictest.screens.views

import android.Manifest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apppppp.bluetoothclassictest.viewmodel.DeviceViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.util.Log

//import androidx.compose.material3.Button
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.draw.drawBehind
var lastData: List<String?> = listOf(null, null, null, null, null, null)

@Composable
fun ReceiveDataView(
    speedD: String?, heiD: String?, rpmD: String?, rollD: String?,
    pitchD: String?, yawD: String?, sw1D: String?, sw2D: String?,
    sw3D: String?, eAngD: String?, rAngD: String?,
    saveData: Boolean, // ← フラグ追加
    deviceViewModel: DeviceViewModel = viewModel()
) {
    val context = LocalContext.current
    // ReceiveDataScreenから呼び出された場合のみデータ保存実行
    if (saveData) {
        LaunchedEffect(speedD, heiD, rpmD, rollD, pitchD, yawD, eAngD, rAngD) {
            saveToSheetIfUpdated(context, speedD, heiD, rpmD, rollD, pitchD, yawD, eAngD, rAngD, sw1D)
        }
    }
    // 高度を Float に変換（null やエラー時は 0m）
    val altitude = heiD?.toFloatOrNull() ?: 0f
    val normalizedAltitude = (altitude / 10f).coerceIn(0.00001f, 1f) // 0.00001 〜 1.0 の範囲に制限(widthの値が0だとエラー出る)

    // 高度に応じて青 (240°) から赤 (0°) への色変化
    val hue = 240f * (1 - normalizedAltitude)  // 高度が高いほど 240 → 0 に変化
    val color = Color.hsv(hue, 1f, 1f) // HSV で指定

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 画像表示を上部に追加
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .border(1.dp, Color.Black)
        ){
            GoogleMapView()//マップ表示
        }

        // 高度
        Text(text = "高度: $heiD m", style = TextStyle(fontSize = 20.sp))
        // 高度バー
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black)
                .size(30.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width * normalizedAltitude
                drawRect(color, size = Size(barWidth, size.height))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 対気速度
        Text(text = "対気速度: $speedD m/s", style = TextStyle(fontSize = 20.sp))
        Spacer(modifier = Modifier.height(16.dp))

        // 回転数
        Text(text = "回転数: $rpmD rpm", style = TextStyle(fontSize = 20.sp))

        Spacer(modifier = Modifier.height(16.dp))

        // 下1/4エリア
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 🔹 残りのスペースを均等に分配
                .border(1.dp, Color.Black)

        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // **左のスペース**
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(1.dp, Color.Black)
                ) {
                    MiniModelView1(pitchD)
                }

                // **右のスペース**
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(1.dp, Color.Black)
                ) {
                    MiniModelView2(rollD)
                }
            }
        }
    }
}

@Composable
fun MiniModelView1(latestDataD: String?) {

    val pitchAngle = latestDataD?.toFloatOrNull() ?: 0f

    Canvas(modifier = Modifier.fillMaxSize()) {
        rotate(pitchAngle, pivot = center) {  // ピッチ角分回転
            drawLine(
                Color.Black, start = Offset(10f, size.height / 2),
                end = Offset(size.width - 50f, size.height / 2), 12f
            )
            drawLine(
                Color.Black,
                start = Offset(60f, size.height / 2 + 50f),
                end = Offset(60f, size.height / 2 - 80f),
                8f
            )
            drawLine(
                Color.Black,
                start = Offset(size.width * 0.72f, size.height / 2),
                end = Offset(
                    size.width * 0.72f,
                    size.height / 2 - 80f
                ),
                8f
            )
            scale(scaleX = 1.5f, scaleY = 1f) {
                drawCircle(
                    Color.Gray, radius = 15.dp.toPx(),
                    center = Offset(
                        size.width * 0.65f,
                        size.height / 2 + 30f
                    )
                )
            }
        }
    }
    Text(text = "ピッチ: $latestDataD °", style = TextStyle(fontSize = 20.sp))
}

@Composable
fun MiniModelView2(latestDataE: String?) {
    val rollAngle = latestDataE?.toFloatOrNull() ?: 0f
    Canvas(modifier = Modifier.fillMaxSize()) {
        rotate(rollAngle, pivot = center) {  // ロール角分回転
            drawLine(
                Color.Black,
                start = Offset(50f, size.height / 2),
                end = Offset(size.width - 50f, size.height / 2),
                strokeWidth = 12f
            )
            drawLine(
                Color.Black,
                start = Offset(size.width / 2, size.width / 2 - 90f),
                end = Offset(size.width / 2, size.height / 2),
                strokeWidth = 8f
            )
            scale(scaleX = 0.8f, scaleY = 1.3f) {
                drawCircle(
                    Color.Gray, radius = 15.dp.toPx(),
                    center = Offset(
                        size.width / 2,
                        size.height / 2 * 1.1f
                    )
                )
            }
        }
    }
    Text(text = "ロール: $latestDataE °", style = TextStyle(fontSize = 20.sp))
}

@SuppressLint("MissingPermission")
fun startLocationUpdates(context: Context, onLocationUpdated: (Location?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
        setMinUpdateIntervalMillis(2000) // 最小更新間隔 2秒
    }.build()

    // 位置情報更新のコールバック
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            onLocationUpdated(location)
        }
    }

    // 位置情報更新のリクエストを開始
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GoogleMapView() {
    val context = LocalContext.current

    // 位置情報パーミッションの状態を監視
    val locationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // 現在地の初期値は "取得待ち" 状態を表す
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // カメラ位置の初期値（仮置き）
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(35.329977, 136.189374), 11.8f)
    }

    // パーミッションのリクエストを実行
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    // パーミッションが許可された場合のみ位置情報を取得
    if (locationPermissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            startLocationUpdates(context) { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    // 現在のズームレベルを取得し、位置のみ更新
                    val currentZoom = cameraPositionState.position.zoom
                    // 地図のカメラ位置を更新
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation!!, currentZoom)
                    cameraPositionState.move(cameraUpdate)
                }
            }
        }
    }

    // Google Map の表示
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = locationPermissionState.status.isGranted
        )
    ) {
        Marker(
            state = rememberMarkerState(position = LatLng(35.682839, 139.759455)),
            title = "折り返し地点",
            snippet = "折り返し地点"
        )
    }

    // パーミッションが拒否された場合のエラーメッセージ
    if (!locationPermissionState.status.isGranted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "位置情報のパーミッションが許可されていません。", color = Color.Red, fontSize = 16.sp)
        }
    }

    // 位置情報の取得待ちのメッセージ
    if (currentLocation == null && locationPermissionState.status.isGranted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "位置情報を取得中...", color = Color.Gray, fontSize = 16.sp)
        }
    }
}

fun saveToSheetIfUpdated(context: Context, speedD: String?, heiD: String?, rpmD: String?, rollD: String?, pitchD: String?, yawD: String?, eAngD:String?, rAngD:String?, sw1D: String?) {
    val newData = listOf(speedD, heiD, rpmD, rollD, pitchD, yawD, eAngD, rAngD)
    if (newData != lastData) {

        // rowDataをList<Any>型で作成（nullもそのまま渡す）
        val rowData: List<Any?> = listOf(
            sw1D,  // ラップ
            speedD,     // speedD (nullがそのまま渡されます)
            heiD,       // heiD (nullがそのまま渡されます)
            rpmD,       // rpmD (nullがそのまま渡されます)
            rollD,      // rollD (nullがそのまま渡されます)
            pitchD,     // pitchD (nullがそのまま渡されます)
            yawD,       // yawD (nullがそのまま渡されます)
            eAngD,      // eAngD (nullがそのまま渡されます)
            rAngD       // rAngD (nullがそのまま渡されます)
        )

        // GoogleSheetsHelper.writeToSheetにcontextとrowDataを渡す
        GoogleSheetsHelper.writeToSheet(rowData)

        // Firebase に送信
        FirebaseHelper.writeToFirebase(rowData)
        lastData = newData
    }
}


