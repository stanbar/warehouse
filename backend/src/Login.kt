package pl.adam

import consentPage
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.Parameters
import io.ktor.request.receiveParameters
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import loginPage
import org.json.simple.JSONObject
import pl.adam.models.ConsentResponse
import pl.adam.models.LoginResponse


fun Routing.getLogin() {
    get("/login") {
        val googleIdToken = call.request.queryParameters["google_id_token"]
        val challenge = call.request.queryParameters["challenge"]!!
        val response: LoginResponse = Hydra.getLoginRequest(challenge)
        when {
            response.skip -> {
                val res =
                    Hydra.acceptLoginRequest<LoginResponse>(challenge, JSONObject(mapOf("subject" to response.subject)))
                call.respondRedirect(url = res.redirectTo)
            }
            googleIdToken != null -> {
                val googleRes = Hydra.authenticateWithGoogle(googleIdToken, challenge)
                call.respondRedirect(url = googleRes.redirectTo)
            }
            else -> {
                call.respondHtml {
                    loginPage(challenge)
                }
            }
        }
    }
}


fun Routing.postLogin() {
    post("login") {
        // The challenge is now a hidden input field, so let's take it from the request body instead
        val challenge = call.request.queryParameters["challenge"]!!
        val email = call.request.queryParameters["email"]!!
        val password = call.request.queryParameters["password"]!!


        val isValidEmployee = email === "adam@gliszczynski.pl" && password === "password";
        val isValidManager = email === "manager@gliszczynski.pl" && password === "password";
        // Let's check if the user provided valid credentials. Of course, you'd use a database or some third-party service
        // for this!
        if (!isValidEmployee && !isValidManager) {
            println("failed to login: ${email} and ${password}")
            // Looks like the user provided invalid credentials, let's show the ui again...
            call.respondHtml {
                loginPage(challenge)
            }
        }

        val response = Hydra.acceptLoginRequest<LoginResponse>(
            challenge,
            JSONObject(mapOf("subject" to email, "remember_for" to "${60 * 60 * 24 * 90}"))
        )
        call.respondRedirect(url = response.redirectTo)
    }
}

fun Routing.getConsent() {
    get("/consent") {
        val challenge = call.request.queryParameters["consent_challenge"]!!
        val response: ConsentResponse = Hydra.getConsentRequest(challenge)
        // If a user has granted this application the requested scope, hydra will tell us to not show the UI.
        if (response.skip) {
            val requestBody = JSONObject(
                mapOf(
                    "grant_scope" to response.requestedScope,
                    "grant_access_token_audience" to response.requestedAccessTokenAudience,
                    "session" to mapOf(
                        "access_token" to mapOf(
                            "role" to if (response.subject.split("@")[0] == "manager") "manager" else "employee"
                        ),
                        "id_token" to mapOf(
                            "role" to if (response.subject.split("@")[0] == "manager") "manager" else "employee"
                        )
                    )
                )
            )
            val res = Hydra.acceptConsentRequest(challenge, requestBody)
            // All we need to do now is to redirect the user back to hydra!
            call.respondRedirect(res.redirect_to)
        } else {
            call.respondHtml {
                consentPage(challenge, response.subject)
            }
        }
    }
}

fun Routing.postConsent() {
    post("/consent") {
        val postParameters: Parameters = call.receiveParameters()
        val challenge = postParameters["challenge"]!!
        if (postParameters["submit"] == "Deny access") {
            val response = Hydra.rejectConsentRequest(
                challenge, JSONObject(
                    mapOf(
                        "error" to "access_denied",
                        "error_description" to "The resource owner denied the request"

                    )
                )
            )
            call.respondRedirect(response.redirect_to)
        } else {
            val response = Hydra.acceptConsentRequest(
                challenge, JSONObject(
                    mapOf(
                        "grant_scope" to postParameters["grant_scope"],
                        "grant_access_token_audience" to postParameters["grant_access_token_audience"],
                        "session" to mapOf(
                            "access_token" to mapOf(
                                "role" to if (postParameters["subject"]!!.split("@")[0] == "manager") "manager" else "employee"
                            ),
                            "id_token" to mapOf(
                                "role" to if (postParameters["usbject"]!!.split("@")[0] == "manager") "manager" else "employee"
                            )
                        ),
                        "remember_for" to 3600
                    )
                )
            )
            call.respondRedirect(response.redirect_to)
        }
    }
}