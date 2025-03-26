import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

object GoogleSheetsHelper {
    private const val SCRIPT_URL = "https://script.google.com/macros/s/AKfycbxtyMzDHha4voVkNxIFCDssaFpGR5pK9O7GsWltk2QNoxnokQey0fyGoNucvVDWkRc/exec"

    fun writeToSheet(rowData: List<Any?>) {
        Thread {
            try {
                val url = URL(SCRIPT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val jsonPayload = JSONObject()
                jsonPayload.put("value", rowData.joinToString(","))

                val outputStream = connection.outputStream
                outputStream.write(jsonPayload.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                Log.d("GoogleSheetsHelper", "Response: $responseCode")
            } catch (e: Exception) {
                Log.e("GoogleSheetsHelper", "Error: ${e.message}", e)
                e.printStackTrace()
            }
        }.start()
    }
}

object FirebaseHelper {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val dataRef: DatabaseReference = database.getReference("sensorData")

    fun writeToFirebase(rowData: List<Any?>) {
        // Firebase 認証済みかをチェック
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val dataMap = mapOf(
                "sw1" to rowData.getOrNull(0),
                "speed" to rowData.getOrNull(1),
                "height" to rowData.getOrNull(2),
                "rpm" to rowData.getOrNull(3),
                "roll" to rowData.getOrNull(4),
                "pitch" to rowData.getOrNull(5),
                "yaw" to rowData.getOrNull(6),
                "eAngle" to rowData.getOrNull(7),
                "rAngle" to rowData.getOrNull(8),
                "timestamp" to System.currentTimeMillis() // ← 追加
            )


            dataRef.push().setValue(dataMap)
                .addOnSuccessListener {
                    Log.d("FirebaseHelper", "🔥 Firebase にデータ送信成功！")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseHelper", "⚠️ Firebase 送信エラー: ${e.message}")
                }
        } else {
            Log.e("FirebaseHelper", "⚠️ ユーザーが認証されていません。")
        }
    }
}

