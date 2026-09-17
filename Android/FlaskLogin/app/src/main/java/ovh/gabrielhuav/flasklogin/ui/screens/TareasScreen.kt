package ovh.gabrielhuav.flasklogin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovh.gabrielhuav.flasklogin.data.model.Tarea
import ovh.gabrielhuav.flasklogin.ui.viewmodel.MainViewModel
import ovh.gabrielhuav.flasklogin.ui.viewmodel.TareasUiState

/**
 * Pantalla de las operaciones CRUD sobre el recurso "Tarea":
 * listar (GET), crear (POST), actualizar (PUT) y borrar (DELETE).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(viewModel: MainViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var tareaEnEdicion by remember { mutableStateOf<Tarea?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadTareas()
    }

    val state = viewModel.tareasState

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                tareaEnEdicion = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state is TareasUiState.Loading && viewModel.tareas.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state is TareasUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadTareas() }) {
                            Text("Reintentar")
                        }
                    }
                }
                viewModel.tareas.isEmpty() -> {
                    Text(
                        text = "No tienes tareas todavia. Toca + para crear una.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.tareas, key = { it.id }) { tarea ->
                            TareaItem(
                                tarea = tarea,
                                onEdit = {
                                    tareaEnEdicion = tarea
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteTarea(tarea) },
                                onToggleCompletada = {
                                    viewModel.updateTarea(
                                        tarea,
                                        tarea.titulo,
                                        tarea.descripcion ?: "",
                                        !tarea.completada
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        TareaDialog(
            tarea = tareaEnEdicion,
            onDismiss = { showDialog = false },
            onConfirm = { titulo, descripcion ->
                val enEdicion = tareaEnEdicion
                if (enEdicion == null) {
                    viewModel.addTarea(titulo, descripcion)
                } else {
                    viewModel.updateTarea(enEdicion, titulo, descripcion, enEdicion.completada)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun TareaItem(
    tarea: Tarea,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompletada: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = tarea.completada, onCheckedChange = { onToggleCompletada() })
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tarea.titulo, style = MaterialTheme.typography.titleMedium)
                if (!tarea.descripcion.isNullOrBlank()) {
                    Text(text = tarea.descripcion, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun TareaDialog(
    tarea: Tarea?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(tarea?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tarea?.descripcion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tarea == null) "Nueva tarea" else "Editar tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Titulo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(titulo.trim(), descripcion.trim()) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
