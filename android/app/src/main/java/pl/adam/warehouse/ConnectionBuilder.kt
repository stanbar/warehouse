package pl.adam.warehouse

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

import net.openid.appauth.Preconditions;
import net.openid.appauth.connectivity.ConnectionBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

object ConnectionBuilder : ConnectionBuilder {
    private const val TAG = "ConnBuilder"

    private val CONNECTION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(15).toInt()
    private val READ_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10).toInt()

    private const val HTTP = "http"
    private const val HTTPS = "https"

    @SuppressLint("TrustAllX509TrustManager")
    private val ANY_CERT_MANAGER: Array<TrustManager> = arrayOf(
        object : X509TrustManager {
            override fun checkClientTrusted(
                p0: Array<out X509Certificate>?,
                p1: String?
            ) {
            }

            override fun checkServerTrusted(
                p0: Array<out X509Certificate>?,
                p1: String?
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }

        }
    )

    @SuppressLint("BadHostnameVerifier")
    private val ANY_HOSTNAME_VERIFIER =
        HostnameVerifier { hostname, session -> true }

    private var TRUSTING_CONTEXT: SSLContext? = null

    init {
        val context: SSLContext? = try {
            SSLContext.getInstance("SSL")
        } catch (e: NoSuchAlgorithmException) {
            Log.e("ConnBuilder", "Unable to acquire SSL context")
            null
        }

        var initializedContext: SSLContext? = null
        if (context != null) {
            try {
                context.init(null, ANY_CERT_MANAGER, SecureRandom())
                initializedContext = context
            } catch (e: KeyManagementException) {
                Log.e(TAG, "Failed to initialize trusting SSL context")
            }
        }

        TRUSTING_CONTEXT = initializedContext

    }

    override fun openConnection(uri: Uri): HttpURLConnection {
        Preconditions.checkNotNull(uri, "url must not be null")

        val conn =
            URL(uri.toString()).openConnection() as HttpURLConnection
        conn.connectTimeout = CONNECTION_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
        conn.instanceFollowRedirects = false

        val httpsConn: HttpsURLConnection = conn as HttpsURLConnection
        httpsConn.sslSocketFactory = TRUSTING_CONTEXT!!.socketFactory
        httpsConn.hostnameVerifier = ANY_HOSTNAME_VERIFIER

        return conn
    }

}