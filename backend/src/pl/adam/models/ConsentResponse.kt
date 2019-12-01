package pl.adam.models

import com.google.gson.annotations.SerializedName


data class ConsentResponse(
    val skip: Boolean,
    val subject: String,
    @SerializedName("requested_scope") val requestedScope: List<String>,
    @SerializedName("requested_access_token_audience") val requestedAccessTokenAudience: List<String>
)
