package ovh.gabrielhuav.flasklogin.data.model

data class Tarea(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val completada: Boolean
)

data class TareaRequest(
    val titulo: String,
    val descripcion: String?,
    val completada: Boolean = false
)
