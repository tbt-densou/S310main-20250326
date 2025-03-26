/*
import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.services.sheets.v4.Sheets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import java.io.IOException
import java.security.GeneralSecurityException
//import com.google.auth.oauth2.GoogleCredentials

object GoogleAuthHelper {

    private val SCOPES = listOf("https://www.googleapis.com/auth/spreadsheets")
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

    const val REQUEST_CODE_SIGN_IN = 1001

    // Google SignInを使ったサインイン処理
    fun signIn(context: Context, activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/spreadsheets")) // Sheetsの権限追加
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    // サインイン後に取得したアクセストークンを利用して、Google Sheets APIサービスオブジェクトを作成
    @Throws(GeneralSecurityException::class, IOException::class)
    fun getSheetsService(context: Context, serverAuthCode: String): Sheets {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Googleアカウントにログインしてください")

        // サインイン後のアクセストークンを取得
        val credentials = GoogleCredentials.fromStream(context.assets.open("path_to_credentials.json"))
            .createScoped(SCOPES)

        // アクセストークンを文字列として取得
        val accessToken = credentials.accessToken.tokenValue

        // BearerTokenとして認証情報をラップ
        val credential: Credential = Credential(BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken(accessToken)

        return Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
            .setApplicationName("com.apppppp.bluetoothclassictest")
            .build()
    }

}*/
