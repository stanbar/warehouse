package pl.adam.warehouse

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration


class MainActivity : AppCompatActivity() {
    val RC_AUTH = 100
    val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnLoginEmail.setOnClickListener {
            authenticateWithEmail()
        }
        btnLoginGoogle.setOnClickListener {
            authenticateWithGoogle()
        }
        if (canRestoreTokens())
            enterApplication()

    }

    private fun authenticateWithGoogle() {
        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse("")
        ) { serviceConfig, ex ->
            when {
                ex != null -> ex.printStackTrace()
                serviceConfig != null -> {
                    val authRequest =
                        AuthorizationRequest.Builder(
                            serviceConfig,
                            "warehouse",
                            ResponseTypeValues.CODE,
                            Uri.parse("pl.adam.warehouse:/oauth2redirect")
                        ).build()
                    doAuthorization(authRequest)

                }
                else -> Log.e(TAG, "Failed to fetch service config from 10.0.2.2:9000")

            }
        }
    }

    private fun authenticateWithEmail() {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://10.0.2.2:9000/oauth2/auth"),  // authorization endpoint
            Uri.parse("https://10.0.2.2:9000/oauth2/token")
        ) // token endpoint

        val authRequest =
            AuthorizationRequest.Builder(
                serviceConfig,
                "warehouse",
                ResponseTypeValues.CODE,
                Uri.parse("pl.adam.warehouse:/oauth2redirect")
            ).build()
        doAuthorization(authRequest)
    }


    private fun canRestoreTokens() = prefs.getString("accessToken", null) != null

    private fun persistTokens(accessToken: String) {
        prefs.edit().putString("accessToken", accessToken).apply()
    }

    private fun enterApplication() {
        startActivity(Intent(this, HomeActivity::class.java))
    }


    private val appAuthConfiguration =
        AppAuthConfiguration.Builder().setConnectionBuilder(ConnectionBuilder).build()
    private val authService by lazy { AuthorizationService(this, appAuthConfiguration) }

    private fun doAuthorization(authRequest: AuthorizationRequest) {
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_AUTH)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_AUTH) {
            AuthorizationException.fromIntent(data)?.let {
                error(it.toJsonString())
            }
            val response = AuthorizationResponse.fromIntent(data!!)!!
            authService.performTokenRequest(response.createTokenExchangeRequest())
            { resp, ex ->
                if (ex != null) {
                    Log.e(TAG, ex.toJsonString())
                } else if (resp != null) { // exchange succeeded
                    val accessToken = resp.accessToken
                    if (accessToken != null) {
                        persistTokens(accessToken)
                        enterApplication()
                    } else {
                        Log.e(TAG, "Accesstoken in response is null")
                    }
                } else {
                    error("Either exception or response should be not null")
                }
            }

        }
    }
}
