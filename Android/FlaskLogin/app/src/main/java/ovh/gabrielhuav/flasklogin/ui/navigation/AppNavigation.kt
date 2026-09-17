package ovh.gabrielhuav.flasklogin.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ovh.gabrielhuav.flasklogin.ui.screens.LoginScreen
import ovh.gabrielhuav.flasklogin.ui.screens.RegisterScreen
import ovh.gabrielhuav.flasklogin.ui.screens.TareasScreen
import ovh.gabrielhuav.flasklogin.ui.viewmodel.MainViewModel

/** Rutas de navegacion de la app. */
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val TAREAS = "tareas"
}

/**
 * Contiene el menu desplegable (Inicio de sesion, Registro, Operaciones CRUD)
 * y el NavHost con las tres pantallas principales de la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: MainViewModel = viewModel()) {

    // Estado de carga inicial: mientras se revisa si ya existe una sesion
    // guardada (DataStore), se muestra un indicador de progreso.
    if (!viewModel.sessionChecked) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val navController = rememberNavController()
    var menuExpanded by remember { mutableStateOf(false) }
    val startDestination = if (viewModel.isLoggedIn) Rutas.TAREAS else Rutas.LOGIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlaskLogin - Tareas") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Inicio de sesion") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Rutas.LOGIN)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Registro de usuario") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Rutas.REGISTRO)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Operaciones CRUD (Tareas)") },
                            onClick = {
                                menuExpanded = false
                                if (viewModel.isLoggedIn) {
                                    navController.navigate(Rutas.TAREAS)
                                } else {
                                    navController.navigate(Rutas.LOGIN)
                                }
                            }
                        )
                        if (viewModel.isLoggedIn) {
                            DropdownMenuItem(
                                text = { Text("Cerrar sesion (${viewModel.currentUsername ?: ""})") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logout()
                                    navController.navigate(Rutas.LOGIN) {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Rutas.TAREAS) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Rutas.REGISTRO) }
                )
            }
            composable(Rutas.REGISTRO) {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.REGISTRO) { inclusive = true }
                        }
                    }
                )
            }
            composable(Rutas.TAREAS) {
                TareasScreen(viewModel = viewModel)
            }
        }
    }
}
