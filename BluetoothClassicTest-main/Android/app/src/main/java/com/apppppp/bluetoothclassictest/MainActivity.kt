package com.apppppp.bluetoothclassictest

// 必要なAndroidおよびJetpack Composeのライブラリをインポート
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.apppppp.bluetoothclassictest.navigation.NavigationGraph
import com.apppppp.bluetoothclassictest.ui.theme.BluetoothClassicTestTheme
import android.provider.Settings
import com.apppppp.bluetoothclassictest.model.BTPermissionHandler
import com.apppppp.bluetoothclassictest.screens.PermissionDeniedSnackbar
import com.apppppp.bluetoothclassictest.viewmodel.DeviceViewModel

// MainActivityクラス。アプリケーションのエントリーポイント。
// アプリケーションが最初に実行される際にこのアクティビティが呼び出されます。
class MainActivity : ComponentActivity() {
    // Bluetoothのパーミッションを管理するハンドラーを宣言
    private lateinit var btPermissionHandler: BTPermissionHandler

    // アクティビティが初めて作成されたときに呼び出されるメソッド
    // Bundleは、アクティビティの状態を保存および復元するために使用されるコンテナです。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // BTPermissionHandlerの初期化
        btPermissionHandler = BTPermissionHandler(
            activity = this,
            onPermissionGranted = {
                loadUI()
            },
            onPermissionDenied = {
                showPermissionDeniedView()
                loadUI() // UI をセットすることでクラッシュを回避
            }
        )

        // パーミッションランチャーのセットアップ
        btPermissionHandler.setupPermissionLauncher()
//        bluetoothPermissionHandler.requestBluetoothPermissions()
    }

    // アクティビティがユーザーと再び対話を始める直前に呼び出されるメソッド
    // ここで再度Bluetoothのパーミッションをリクエストします。
    override fun onResume() {
        super.onResume()
        btPermissionHandler.requestBluetoothPermissions()
    }

    // UIを読み込むメソッド
    // パーミッションが許可された場合にUIを設定します。
    private fun loadUI() {
        setContent {
            // ナビゲーションコントローラの作成
            val navController = rememberNavController()
            // アプリのテーマを設定
            BluetoothClassicTestTheme {
                // ナビゲーショングラフを設定
                NavigationGraph(
                    navController = navController,
                    // Bluetooth設定画面を開く関数の設定
                    openBluetoothSettings = { openBluetoothSettings() },
                    deviceViewModel = DeviceViewModel()
                )
            }
        }
    }

    // パーミッションが拒否された場合に呼び出されるビューを表示するメソッド
    private fun showPermissionDeniedView() {
        setContent {
            // スナックバーを表示し、設定画面を開くオプションを提供
            PermissionDeniedSnackbar(onSettingsClick = { openAppSettings() })
        }
    }

    // アプリの設定画面を開くメソッド
    private fun openAppSettings() {
        // 設定画面を開くIntentを作成
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    // Bluetooth設定画面を開くメソッド
    private fun openBluetoothSettings() {
        // Bluetooth設定画面を開くIntentを作成
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intent)
    }

}