package ovh.gabrielhuav.flasklogin.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // IMPORTANTE (ver README):
    // - Emulador de Android Studio -> "10.0.2.2" apunta al localhost de tu PC,
    //   donde corre el contenedor de Docker (puerto 5000 publicado).
    // - Dispositivo fisico en la misma red -> reemplaza esto por la IP local
    //   de tu computadora, por ejemplo "http://192.168.1.50:5000/".
    private const val BASE_URL = "http://10.0.2.2:5000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
