package pl.adam.warehouse.utils

import android.annotation.SuppressLint;
import android.net.Uri;

import net.openid.appauth.Preconditions;
import net.openid.appauth.connectivity.ConnectionBuilder;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

object ConnectionBuilder : ConnectionBuilder {
    private const val TAG = "ConnBuilder"

    private val CONNECTION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(15).toInt()
    private val READ_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10).toInt()

    private val ANY_CERT_MANAGER: Array<TrustManager> = arrayOf(
        @SuppressLint("TrustAllX509TrustManager")
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
        HostnameVerifier { _, _ -> true }

    private var TRUSTING_CONTEXT: SSLContext? = SSLContext.getInstance("SSL").apply {
        init(null,
            ANY_CERT_MANAGER, SecureRandom())
    }

    override fun openConnection(uri: Uri): HttpURLConnection {
        Preconditions.checkNotNull(uri, "url must not be null")

        val conn =
            URL(uri.toString()).openConnection() as HttpURLConnection
        conn.connectTimeout =
            CONNECTION_TIMEOUT_MS
        conn.readTimeout =
            READ_TIMEOUT_MS
        conn.instanceFollowRedirects = false

        val httpsConn: HttpsURLConnection = conn as HttpsURLConnection
        httpsConn.sslSocketFactory = TRUSTING_CONTEXT!!.socketFactory
        httpsConn.hostnameVerifier =
            ANY_HOSTNAME_VERIFIER

        return conn
    }

}