package ovh.gabrielhuav.flasklogin.data.network

import ovh.gabrielhuav.flasklogin.data.model.LoginRequest
import ovh.gabrielhuav.flasklogin.data.model.LoginResponse
import ovh.gabrielhuav.flasklogin.data.model.RegisterRequest
import ovh.gabrielhuav.flasklogin.data.model.RegisterResponse
import ovh.gabrielhuav.flasklogin.data.model.Tarea
import ovh.gabrielhuav.flasklogin.data.model.TareaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("tareas")
    suspend fun getTareas(@Header("Authorization") token: String): Response<List<Tarea>>

    @POST("tareas")
    suspend fun createTarea(
        @Header("Authorization") token: String,
        @Body request: TareaRequest
    ): Response<Tarea>

    @PUT("tareas/{id}")
    suspend fun updateTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: TareaRequest
    ): Response<Tarea>

    @DELETE("tareas/{id}")
    suspend fun deleteTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}
