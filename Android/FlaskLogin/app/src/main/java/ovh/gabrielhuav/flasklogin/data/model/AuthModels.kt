package ovh.gabrielhuav.flasklogin.data.model

data class RegisterRequest(
    val username: String,
    val password: String
)

data class RegisterResponse(
    val message: String? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val access_token: String? = null,
    val user_id: Int? = null,
    val username: String? = null
)

data class ErrorResponse(
    val message: String? = null,
    val status: String? = null
)
