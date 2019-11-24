package pl.adam.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ConsentResponse(
    val skip: Boolean,
    val subject: String,
    @SerialName("requested_scope") val requestedScope: List<String>,
    @SerialName("requested_access_token_audience") val requestedAccessTokenAudience: List<String>
)
