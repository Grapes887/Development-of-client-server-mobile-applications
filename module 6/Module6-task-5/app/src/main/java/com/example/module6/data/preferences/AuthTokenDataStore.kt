package com.example.module6.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val AUTH_DATASTORE_NAME = "auth_token_store"
private val Context.authDataStore by preferencesDataStore(name = AUTH_DATASTORE_NAME)

class AuthTokenDataStore(
    private val context: Context
) {
    suspend fun saveToken(token: String) {
        context.authDataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = token
        }
    }

    suspend fun readToken(): String? {
        return context.authDataStore.data.map { preferences ->
            preferences[Keys.ACCESS_TOKEN]
        }.first()
    }

    suspend fun clearToken() {
        context.authDataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
        }
    }

    private object Keys {
        val ACCESS_TOKEN: Preferences.Key<String> = stringPreferencesKey("access_token")
    }
}
