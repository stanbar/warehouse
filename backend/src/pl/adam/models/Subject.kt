package pl.adam.models

import com.google.gson.annotations.SerializedName


data class Subject(
    val active: Boolean,
    @SerializedName("sub")
    val subject: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("exp")
    val expiration: Long,
    @SerializedName("iat")
    val issuedAt: Long,
    @SerializedName("iss")
    val issuer: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("ext")
    val extra: Map<String, String>
)
