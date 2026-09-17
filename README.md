# Flask Login API + Android Client

Este repositorio contiene dos componentes: un backend REST construido con Flask y dockerizado, y una app Android base en Kotlin para consumirlo.

---

## Backend — `Docker-Flask/ORM/`

API REST minimalista para registro e inicio de sesión de usuarios. Usa **Flask-SQLAlchemy** para persistencia con SQLite y **Flask-Bcrypt** para hashear contraseñas. No requiere configurar una base de datos externa; el archivo `site.db` se crea automáticamente al iniciar.

### Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/` | Verifica que la API está activa |
| `POST` | `/register` | Registra un nuevo usuario |
| `POST` | `/login` | Autentica un usuario existente |

#### `POST /register`

```json
// Request
{ "username": "alice", "password": "secreto123" }

// Response 201
{ "message": "Usuario creado exitosamente" }

// Response 400 (usuario duplicado)
{ "message": "El usuario ya existe" }
```

#### `POST /login`

```json
// Request
{ "username": "alice", "password": "secreto123" }

// Response 200
{ "status": "success", "message": "Login exitoso", "user_id": 1, "username": "alice" }

// Response 401
{ "status": "error", "message": "Credenciales inválidas" }
```

### Levantar con Docker

```bash
cd Docker-Flask/ORM
docker compose up --build
```

El servicio queda disponible en `http://localhost:5000`. Si modificas `app.py` mientras el contenedor está corriendo, Flask recarga automáticamente gracias al volumen montado y al modo debug.

### Probar con curl

```bash
curl http://localhost:5000/

curl -X POST http://localhost:5000/register \
     -H "Content-Type: application/json" \
     -d "{\"username\":\"android_dev\",\"password\":\"mi_password_secreto\"}"

curl -X POST http://localhost:5000/login \
     -H "Content-Type: application/json" \
     -d "{\"username\":\"android_dev\",\"password\":\"mi_password_secreto\"}"
```

### Stack

- Python 3.9 (imagen `python:3.9-slim`)
- Flask · Flask-SQLAlchemy · Flask-Bcrypt
- SQLite (archivo local, no requiere servicio externo)
- Docker Compose

---

## App Android — `Android/FlaskLogin/`

Proyecto base generado con Android Studio usando **Kotlin** y **Jetpack Compose** (Material 3). El `MainActivity.kt` es el punto de partida; la lógica de conexión con la API queda pendiente de implementar.

**Configuración:** `minSdk 24`, `targetSdk 36`.

### Conectar al backend desde el emulador

El emulador AVD no resuelve `localhost` de la máquina host. Usa `http://10.0.2.2:5000` en su lugar. Con un dispositivo físico en la misma red, usa la IP local de tu PC.

Agrega en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Y dentro de `<application>`:

```xml
android:usesCleartextTraffic="true"
```
