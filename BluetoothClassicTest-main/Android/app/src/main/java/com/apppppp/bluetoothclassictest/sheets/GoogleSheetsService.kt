package com.apppppp.bluetoothclassictest.sheets

import android.content.Context
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory // 修正: GoogleJsonFactory → GsonFactory
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.sheets.v4.Sheets
import java.io.InputStream

object GoogleSheetsService {
    fun getSheetsService(context: Context): Sheets {
        val assetManager = context.assets
        val inputStream: InputStream = assetManager.open("credentials.json")

        val credential = GoogleCredential.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))

        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory(), // 変更: GoogleJsonFactory → GsonFactory
            credential
        ).setApplicationName("BluetoothClassicTest").build()
    }
}
