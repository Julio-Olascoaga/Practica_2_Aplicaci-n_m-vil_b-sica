package ovh.gabrielhuav.flasklogin.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import ovh.gabrielhuav.flasklogin.data.local.SessionManager
import ovh.gabrielhuav.flasklogin.data.model.LoginRequest
import ovh.gabrielhuav.flasklogin.data.model.RegisterRequest
import ovh.gabrielhuav.flasklogin.data.model.Tarea
import ovh.gabrielhuav.flasklogin.data.model.TareaRequest
import ovh.gabrielhuav.flasklogin.data.network.RetrofitClient
import java.io.IOException

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class TareasUiState {
    object Idle : TareasUiState()
    object Loading : TareasUiState()
    object Success : TareasUiState()
    data class Error(val message: String) : TareasUiState()
}

/**
 * ViewModel unico de la app: maneja autenticacion (registro/login/logout,
 * persistiendo el token JWT con DataStore) y las operaciones CRUD de tareas.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val api = RetrofitClient.apiService

    private var authToken: String? = null

    var sessionChecked by mutableStateOf(false)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var currentUsername by mutableStateOf<String?>(null)
        private set

    var authState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    var tareasState by mutableStateOf<TareasUiState>(TareasUiState.Idle)
        private set

    var tareas by mutableStateOf<List<Tarea>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            val savedToken = sessionManager.getToken()
            if (!savedToken.isNullOrBlank()) {
                authToken = savedToken
                currentUsername = sessionManager.getUsername()
                isLoggedIn = true
            }
            sessionChecked = true
        }
    }

    fun resetAuthState() {
        authState = AuthUiState.Idle
    }

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            authState = AuthUiState.Error("Usuario y contrasena son obligatorios")
            return
        }
        viewModelScope.launch {
            authState = AuthUiState.Loading
            try {
                val response = api.register(RegisterRequest(username, password))
                authState = if (response.isSuccessful) {
                    AuthUiState.Success
                } else {
                    AuthUiState.Error(parseError(response.errorBody()?.string(), "No se pudo registrar el usuario"))
                }
            } catch (e: IOException) {
                authState = AuthUiState.Error("No se pudo conectar con el servidor. Verifica que el backend este corriendo.")
            } catch (e: Exception) {
                authState = AuthUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            authState = AuthUiState.Error("Usuario y contrasena son obligatorios")
            return
        }
        viewModelScope.launch {
            authState = AuthUiState.Loading
            try {
                val response = api.login(LoginRequest(username, password))
                val body = response.body()
                if (response.isSuccessful && body?.access_token != null) {
                    authToken = body.access_token
                    currentUsername = body.username ?: username
                    sessionManager.saveSession(authToken!!, currentUsername!!)
                    isLoggedIn = true
                    authState = AuthUiState.Success
                } else {
                    authState = AuthUiState.Error(parseError(response.errorBody()?.string(), "Credenciales invalidas"))
                }
            } catch (e: IOException) {
                authState = AuthUiState.Error("No se pudo conectar con el servidor. Verifica que el backend este corriendo.")
            } catch (e: Exception) {
                authState = AuthUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            authToken = null
            currentUsername = null
            isLoggedIn = false
            tareas = emptyList()
            authState = AuthUiState.Idle
            tareasState = TareasUiState.Idle
        }
    }

    private fun authHeader(): String = "Bearer $authToken"

    fun loadTareas() {
        val token = authToken ?: return
        viewModelScope.launch {
            tareasState = TareasUiState.Loading
            try {
                val response = api.getTareas(authHeader())
                if (response.isSuccessful) {
                    tareas = response.body() ?: emptyList()
                    tareasState = TareasUiState.Success
                } else if (response.code() == 401) {
                    logout()
                    tareasState = TareasUiState.Error("Tu sesion expiro, vuelve a iniciar sesion")
                } else {
                    tareasState = TareasUiState.Error("No se pudieron cargar las tareas")
                }
            } catch (e: IOException) {
                tareasState = TareasUiState.Error("No se pudo conectar con el servidor")
            } catch (e: Exception) {
                tareasState = TareasUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun addTarea(titulo: String, descripcion: String) {
        if (authToken == null) return
        if (titulo.isBlank()) {
            tareasState = TareasUiState.Error("El titulo es obligatorio")
            return
        }
        viewModelScope.launch {
            tareasState = TareasUiState.Loading
            try {
                val response = api.createTarea(authHeader(), TareaRequest(titulo, descripcion, false))
                if (response.isSuccessful) {
                    loadTareas()
                } else {
                    tareasState = TareasUiState.Error("No se pudo crear la tarea")
                }
            } catch (e: IOException) {
                tareasState = TareasUiState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun updateTarea(tarea: Tarea, nuevoTitulo: String, nuevaDescripcion: String, completada: Boolean) {
        if (authToken == null) return
        if (nuevoTitulo.isBlank()) {
            tareasState = TareasUiState.Error("El titulo es obligatorio")
            return
        }
        viewModelScope.launch {
            tareasState = TareasUiState.Loading
            try {
                val response = api.updateTarea(
                    authHeader(),
                    tarea.id,
                    TareaRequest(nuevoTitulo, nuevaDescripcion, completada)
                )
                if (response.isSuccessful) {
                    loadTareas()
                } else {
                    tareasState = TareasUiState.Error("No se pudo actualizar la tarea")
                }
            } catch (e: IOException) {
                tareasState = TareasUiState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun deleteTarea(tarea: Tarea) {
        if (authToken == null) return
        viewModelScope.launch {
            tareasState = TareasUiState.Loading
            try {
                val response = api.deleteTarea(authHeader(), tarea.id)
                if (response.isSuccessful) {
                    loadTareas()
                } else {
                    tareasState = TareasUiState.Error("No se pudo eliminar la tarea")
                }
            } catch (e: IOException) {
                tareasState = TareasUiState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    private fun parseError(body: String?, fallback: String): String {
        if (body.isNullOrBlank()) return fallback
        return try {
            val obj = JsonParser.parseString(body).asJsonObject
            obj.get("message")?.asString ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
