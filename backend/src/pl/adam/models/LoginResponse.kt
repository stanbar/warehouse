package pl.adam.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("redirect_to")
    val redirectTo: String,
    @SerialName("request_url")
    val requestUrl: String,
    val subject: String,
    val skip: Boolean
)
