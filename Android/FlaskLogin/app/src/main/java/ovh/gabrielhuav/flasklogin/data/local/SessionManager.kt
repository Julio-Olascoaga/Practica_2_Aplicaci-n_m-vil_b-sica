package ovh.gabrielhuav.flasklogin.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

/**
 * Guarda el token de sesion (JWT) y el nombre de usuario en almacenamiento
 * local (DataStore) para que la sesion persista entre ejecuciones de la app.
 */
class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }

    suspend fun saveSession(token: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USERNAME_KEY] = username
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.first()[TOKEN_KEY]

    suspend fun getUsername(): String? = context.dataStore.data.first()[USERNAME_KEY]

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
