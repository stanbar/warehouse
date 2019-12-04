package pl.adam.warehouse

import android.content.SharedPreferences

class LocalStorage(private val prefs: SharedPreferences) {
    fun getAccessToken() = prefs.getString("accessToken", null)
    fun clearAccessToken() = prefs.edit().putString("accessToken", null).apply()
    fun canRestoreTokens() = prefs.getString("accessToken", null) != null
    fun persistTokens(accessToken: String) {
        prefs.edit().putString("accessToken", accessToken).apply()
    }

}