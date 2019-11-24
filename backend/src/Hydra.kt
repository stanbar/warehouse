package pl.adam

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.http.headersOf
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContextBuilder
import org.json.simple.JSONObject
import pl.adam.models.LoginResponse
import java.net.URL

val hydraUrl = System.getenv("HYDRA_ADMIN_URL")

val client = HttpClient(Apache) {
    install(JsonFeature)
    engine {
        customizeClient {
            setSSLContext(
                SSLContextBuilder
                    .create()
                    .loadTrustMaterial(TrustSelfSignedStrategy())
                    .build()
            )
            setSSLHostnameVerifier(NoopHostnameVerifier())
        }
    }
}

object Hydra {
    // Fetches information on a login request.
    suspend inline fun <reified T> getLoginRequest(challenge: String): T {
        return get("login", challenge)
    }

    // Accepts a login request.
    suspend inline fun <reified T> acceptLoginRequest(challenge: String, body: JSONObject): T {
        return put("login", "accept", challenge, body);
    }

    // Rejects a login request.
    suspend inline fun <reified T> rejectLoginRequest(challenge: String, body: JSONObject): T {
        return put("login", "reject", challenge, body);
    }

    // Fetches information on a consent request.
    suspend inline fun <reified T> getConsentRequest(challenge: String): T {
        return get("consent", challenge);
    }

    // Accepts a consent request.
    class ConsentResponse(val redirect_to: String)

    suspend inline fun acceptConsentRequest(challenge: String, body: JSONObject): ConsentResponse {
        return put("consent", "accept", challenge, body);
    }

    // Rejects a consent request.
    suspend inline fun rejectConsentRequest(challenge: String, body: JSONObject): ConsentResponse {
        return put("consent", "reject", challenge, body);
    }

    // Fetches information on a logout request.
    suspend inline fun <reified T> getLogoutRequest(challenge: String): T {
        return get("logout", challenge);
    }

    // Accepts a logout request.
    suspend inline fun <reified T> acceptLogoutRequest(challenge: String): T {
        return put("logout", "accept", challenge, JSONObject())
    }

    // Reject a logout request.
    suspend inline fun <reified T> rejectLogoutRequest(challenge: String): T {
        return put("logout", "reject", challenge, JSONObject())
    }

    suspend inline fun authenticateWithGoogle(
        googleIdToken: String,
        challenge: String
    ): LoginResponse {
        val tokenInfoUrl = URL("https://oauth2.googleapis.com/tokeninfo?id_token=$googleIdToken")
        val token: Map<String, String> = client.get(tokenInfoUrl)
        validateGoogleToken(token);
        val body = JSONObject(
            mapOf(
                "subject" to token["email"]!!,
                "remember" to "${3600 * 24 * 90}"
            )
        )
        return Hydra.acceptLoginRequest(challenge, body)
    }

    fun validateGoogleToken(token: Map<String, String>) {
        println(token)
        val iss = token["iss"] ?: error("could not find iss field in id_token")
        if (iss !== "https://accounts.google.com" && iss !== "accounts.google.com") {
            throw error("Invalid iss $iss")
        }

        val exp = token["exp"] ?: throw error("could not find exp field in id_token")

        if (exp.toInt() < System.currentTimeMillis()) {
            error("token expired")
        }
    }

    // A little helper that takes type (can be "login" or "consent") and a challenge and returns the response from ORY Hydra.
    suspend inline fun <reified T> get(flow: String, challenge: String): T {
        if (flow !in arrayOf("login", "consent")) {
            error("invalid flow")
        }

        val url = URL("$hydraUrl/oauth2/auth/requests/$flow?${flow}_challenge=$challenge")
        println("GET: $url")
        return client.get(url) {
            headersOf("Content-Type", "application/json")
        }
    }

    // A little helper that takes type (can be "login" or "consent"), the action (can be "accept" or "reject") and a challenge and returns the response from ORY Hydra.
    suspend inline fun <reified T> put(flow: String, action: String, challenge: String, bodyJson: JSONObject): T {
        if (flow !in arrayOf("login", "consent")) {
            error("invalid flow")
        }

        val url = URL("$hydraUrl/oauth2/auth/requests/$flow/$action?${flow}_challenge=$challenge")
        println("PUT: $url")
        return client.put(url) {
            header("Content-Type", "application/json")
            body = bodyJson.toJSONString()
        }
    }
}


