package pl.adam.warehouse

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration
import pl.adam.warehouse.utils.ConnectionBuilder


class MainActivity : AppCompatActivity() {
    val RC_EMAIL_AUTH = 100
    val RC_GOOGLE_AUTH = 200
    val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    val TAG = "MainActivity"

    private val localStorage by lazy { LocalStorage(prefs) }
    private val appAuthConfiguration =
        AppAuthConfiguration.Builder().setConnectionBuilder(ConnectionBuilder).build()
    private val authService by lazy { AuthorizationService(this, appAuthConfiguration) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnLoginEmail.setOnClickListener {
            authenticateWithEmail()
        }
        btnLoginGoogle.setOnClickListener {
            authenticateWithGoogle()
        }
        if (localStorage.canRestoreTokens())
            enterApplication()

    }

    private fun authenticateWithGoogle() {
        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse("https://accounts.google.com")
        ) { serviceConfig, ex ->
            when {
                ex != null -> ex.printStackTrace()
                serviceConfig != null -> {
                    val authRequest =
                        AuthorizationRequest.Builder(
                            serviceConfig,
                            "955974405187-spkkfkken9ejn2so5leqbc1nate61ci0.apps.googleusercontent.com",
                            ResponseTypeValues.CODE,
                            Uri.parse("pl.adam.warehouse:/oauth2redirect")
                        )
                            .setScopes("openid", "email", "profile")
                            .build()

                    doGoogleAuthorization(authRequest)

                }
                else -> Log.e(TAG, "Failed to fetch service config from 10.0.2.2:9000")

            }
        }
    }

    private fun authenticateWithEmail(googleIdToken: String? = null) {
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
            )
                .setScopes("openid", "offline")
                .apply {
                    if (googleIdToken != null)
                        setAdditionalParameters(mapOf("google_id_token" to googleIdToken))
                }
                .build()
        Log.d(TAG, authRequest.jsonSerializeString())
        doEmailAuthorization(authRequest)
    }

    private fun enterApplication() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private fun doGoogleAuthorization(authRequest: AuthorizationRequest) {
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_GOOGLE_AUTH)
    }

    private fun doEmailAuthorization(authRequest: AuthorizationRequest) {
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_EMAIL_AUTH)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_EMAIL_AUTH) {
            val exception = AuthorizationException.fromIntent(data)
            if (exception  != null) {
                Toast.makeText(this, exception.toJsonString(), Toast.LENGTH_SHORT).show()
                return
            }
            val response = AuthorizationResponse.fromIntent(data!!)!!
            authService.performTokenRequest(response.createTokenExchangeRequest())
            { resp, ex ->
                if (ex != null) {
                    Log.e(TAG, ex.toJsonString())
                } else if (resp != null) { // exchange succeeded
                    val accessToken = resp.accessToken
                    if (accessToken != null) {
                        localStorage.persistTokens(accessToken)
                        enterApplication()
                    } else {
                        Log.e(TAG, "Access token in hydra response is null")
                    }
                } else {
                    error("Either exception or response should be not null")
                }
            }
        } else if (requestCode == RC_GOOGLE_AUTH) {
            val exception = AuthorizationException.fromIntent(data)
            if (exception  != null) {
                Toast.makeText(this, exception.toJsonString(), Toast.LENGTH_SHORT).show()
                return
            }

            val response = AuthorizationResponse.fromIntent(data!!)!!
            authService.performTokenRequest(response.createTokenExchangeRequest())
            { resp, ex ->
                if (ex != null) {
                    Log.e(TAG, ex.toJsonString())
                } else if (resp != null) { // exchange succeeded
                    val idToken = resp.idToken
                    if (idToken != null) {
                        authenticateWithEmail(idToken)
                    } else {
                        Log.e(TAG, "Access token in google response is null")
                    }
                } else {
                    error("Either exception or response should be not null")
                }
            }
        }
    }
}
